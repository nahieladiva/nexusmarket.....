package application.adapters.in.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.adapters.out.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.adapters.out.persistence.mongodb.repositories.IssuedTokenMongoRepository;
import application.adapters.out.persistence.mongodb.repositories.StockMovementMongoRepository;
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
}
