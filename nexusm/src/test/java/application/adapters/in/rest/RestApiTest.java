package application.adapters.in.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.adapters.out.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.adapters.out.persistence.mongodb.repositories.IssuedTokenMongoRepository;
import application.adapters.out.persistence.mongodb.repositories.StockMovementMongoRepository;
import application.domain.enums.UserRole;
import application.domain.ports.out.StockMovementLogPort;
import application.domain.ports.out.TokenStorePort;
import application.support.H2WithoutChecksDialect;
import application.support.InMemoryStockMovementLog;
import application.support.InMemoryTokenStore;
import com.jayway.jsonpath.JsonPath;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Base of the REST tests. They use the real context: security, controllers, use cases, domain and
 * JPA adapters on H2. Only MongoDB is replaced, by in-memory token and stock movement stores.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:rest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    H2WithoutChecksDialect.PROPERTY
})
@AutoConfigureMockMvc
@Import(RestApiTest.InMemoryMongoPorts.class)
public abstract class RestApiTest {

    protected static final String ADMIN_EMAIL = "admin@nexusmarket.com";
    protected static final String ADMIN_PASSWORD = "Admin2026";
    protected static final String PASSWORD = "Password2026";
    protected static final String ADDRESS = """
        {"street":"10th Street","city":"Springfield","state":"Oregon",
         "zipCode":"97477","country":"United States"}""";
    protected static final String LOCATION = """
        {"aisle":"A","shelf":"1","bin":"1"}""";

    private static int sequence;

    @Autowired
    protected MockMvc mvc;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private MongoTemplate mongoTemplate;

    @MockitoBean
    private AuditLogMongoRepository auditLogMongoRepository;

    @MockitoBean
    private IssuedTokenMongoRepository issuedTokenMongoRepository;

    @MockitoBean
    private StockMovementMongoRepository stockMovementMongoRepository;

    @TestConfiguration
    static class InMemoryMongoPorts {

        @Bean
        @Primary
        TokenStorePort inMemoryTokenStore() {
            return new InMemoryTokenStore();
        }

        @Bean
        @Primary
        StockMovementLogPort inMemoryStockMovementLog() {
            return new InMemoryStockMovementLog();
        }
    }

    /** The tests share one database, so their data must not collide. */
    protected static synchronized int next() {
        return ++sequence;
    }

    protected static String uniqueEmail(String prefix) {
        return prefix + next() + "@mail.com";
    }

    protected ResultActions send(MockHttpServletRequestBuilder request, String token, String body)
            throws Exception {
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mvc.perform(request);
    }

    protected ResultActions send(MockHttpServletRequestBuilder request, String token) throws Exception {
        return send(request, token, null);
    }

    protected static String read(ResultActions result, String path) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), path);
    }

    protected String login(String email, String password) throws Exception {
        return read(send(post("/api/v1/public/login"), null,
            "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password))
            .andExpect(status().isOk()), "$.token");
    }

    protected String adminToken() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    /** Registers a buyer and returns its email. */
    protected String registerBuyer() throws Exception {
        String email = uniqueEmail("buyer");
        send(post("/api/v1/public/buyers"), null, """
            {"fullName":"Buyer","email":"%s","phone":"+573001112233","password":"%s",
             "defaultShippingAddress":%s}""".formatted(email, PASSWORD, ADDRESS))
            .andExpect(status().isCreated());
        return email;
    }

    /** Registers an approved seller and returns its email. */
    protected String registerSeller() throws Exception {
        String admin = adminToken();
        String email = uniqueEmail("seller");
        String id = read(send(post("/api/v1/admin/sellers"), admin, """
            {"fullName":"Seller","email":"%s","phone":"+573001112233","password":"%s",
             "businessName":"Store"}""".formatted(email, PASSWORD))
            .andExpect(status().isCreated()), "$.id");
        send(post("/api/v1/admin/sellers/" + id + "/approve"), admin).andExpect(status().isOk());
        return email;
    }

    /** Registers internal staff with the given role and returns its email. */
    protected String registerStaff(UserRole role) throws Exception {
        String email = uniqueEmail("staff");
        send(post("/api/v1/admin/staff"), adminToken(), """
            {"role":"%s","fullName":"Staff","email":"%s","phone":"+573001112233","password":"%s"}"""
            .formatted(role, email, PASSWORD))
            .andExpect(status().isCreated());
        return email;
    }

    /** Creates a warehouse of the seller and returns its id. */
    protected String createWarehouse(String sellerToken) throws Exception {
        return read(send(post("/api/v1/seller/warehouses"), sellerToken, """
            {"name":"Warehouse %d","address":%s,"location":%s}""".formatted(next(), ADDRESS, LOCATION))
            .andExpect(status().isCreated()), "$.id");
    }

    /** Registers a physical product of the seller, still unpublished, and returns its id. */
    protected String createProduct(String sellerToken) throws Exception {
        return read(send(post("/api/v1/seller/products"), sellerToken, """
            {"code":"TEC-%06d","type":"PHYSICAL","name":"Headphones","description":"Bluetooth",
             "price":{"amount":"150000.00","currency":"COP"}}""".formatted(next()))
            .andExpect(status().isCreated()), "$.id");
    }

    /** Registers a product, publishes it, and stores units in a new warehouse. */
    protected StockedProduct stockedProduct(String sellerToken, int units) throws Exception {
        String productId = createProduct(sellerToken);
        String warehouseId = createWarehouse(sellerToken);
        send(post("/api/v1/seller/products/" + productId + "/publish"), sellerToken)
            .andExpect(status().isOk());
        send(post("/api/v1/seller/inventory"), sellerToken, """
            {"productId":"%s","warehouseId":"%s","onHand":%d,"reorderThreshold":1,"location":%s}"""
            .formatted(productId, warehouseId, units, LOCATION))
            .andExpect(status().isCreated());
        return new StockedProduct(productId, warehouseId);
    }

    /** Puts a unit of the product in the cart of the buyer and places the order. Returns its id. */
    protected String placeOrder(String buyerToken, String productId) throws Exception {
        send(post("/api/v1/buyer/cart/items"), buyerToken,
            "{\"productId\":\"%s\",\"quantity\":1}".formatted(productId))
            .andExpect(status().isOk());
        return read(send(post("/api/v1/buyer/orders"), buyerToken)
            .andExpect(status().isCreated()), "$.id");
    }

    /** A published product with stock, and the warehouse that holds it. */
    protected record StockedProduct(String productId, String warehouseId) {
    }

    /** Logs in a new user of the given role. */
    protected String tokenFor(UserRole role) throws Exception {
        return switch (role) {
            case ADMINISTRATOR -> adminToken();
            case BUYER -> login(registerBuyer(), PASSWORD);
            case SELLER -> login(registerSeller(), PASSWORD);
            case LOGISTICS_OPERATOR, SUPERVISOR -> login(registerStaff(role), PASSWORD);
        };
    }
}
