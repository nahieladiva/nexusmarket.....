package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import application.adapters.out.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.adapters.out.persistence.mongodb.repositories.IssuedTokenMongoRepository;
import application.adapters.out.persistence.mongodb.repositories.StockMovementMongoRepository;
import application.domain.enums.UserRole;
import application.domain.models.Administrator;
import application.domain.models.User;
import application.domain.ports.in.AccountPort;
import application.domain.ports.in.AdministratorPort;
import application.domain.ports.in.BuyerPort;
import application.domain.ports.in.LogisticsOperatorPort;
import application.domain.ports.in.PublicAccessPort;
import application.domain.ports.in.SellerPort;
import application.domain.ports.in.SupervisorPort;
import application.domain.ports.out.AuditLogPort;
import application.domain.ports.out.CartRepository;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.DomainEventPublisherPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.InvoiceRepository;
import application.domain.ports.out.NotificationPort;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.PasswordEncoderPort;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.RefundRepository;
import application.domain.ports.out.ReturnRequestRepository;
import application.domain.ports.out.ShipmentRepository;
import application.domain.ports.out.StockMovementLogPort;
import application.domain.ports.out.TokenProviderPort;
import application.domain.ports.out.TokenStorePort;
import application.domain.ports.out.UserRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Email;
import application.support.H2WithoutChecksDialect;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Starts the full Spring context without external databases: H2 replaces MySQL and the MongoDB
 * beans are mocks. The real MySQL and MongoDB are checked by starting the application with Docker.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:context;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    H2WithoutChecksDialect.PROPERTY
})
class NexusmarketApplicationTests {

    private static final int DOMAIN_SERVICES = 71;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private MongoTemplate mongoTemplate;

    @MockitoBean
    private AuditLogMongoRepository auditLogMongoRepository;

    @MockitoBean
    private IssuedTokenMongoRepository issuedTokenMongoRepository;

    @MockitoBean
    private StockMovementMongoRepository stockMovementMongoRepository;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private UserRepository users;

    @Autowired
    private DataSource dataSource;

    @Test
    void everyDomainServiceIsABean() {
        assertEquals(DOMAIN_SERVICES, context.getBeansWithAnnotation(DomainService.class).size());
    }

    @Test
    void everyInputPortHasItsUseCase() {
        List.of(PublicAccessPort.class, AccountPort.class, BuyerPort.class, SellerPort.class,
                LogisticsOperatorPort.class, AdministratorPort.class, SupervisorPort.class)
            .forEach(port -> assertEquals(1, context.getBeansOfType(port).size(), port.getName()));
    }

    @Test
    void everyOutputPortHasItsAdapter() {
        List.of(AuditLogPort.class, CartRepository.class, ClockPort.class,
                DomainEventPublisherPort.class, IdGeneratorPort.class, InventoryRepository.class,
                InvoiceRepository.class, NotificationPort.class, OrderRepository.class,
                PasswordEncoderPort.class, ProductRepository.class, RefundRepository.class,
                ReturnRequestRepository.class, ShipmentRepository.class, StockMovementLogPort.class,
                TokenProviderPort.class, TokenStorePort.class, UserRepository.class,
                WarehouseRepository.class)
            .forEach(port -> assertEquals(1, context.getBeansOfType(port).size(), port.getName()));
    }

    @Test
    void theTablesOfTheNewAggregatesExist() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        List.of("carts", "cart_items", "invoices", "shipments", "return_requests", "refunds")
            .forEach(table -> assertEquals(0,
                jdbc.queryForObject("select count(*) from " + table, Integer.class), table));
    }

    @Test
    void theInitialAdministratorIsCreatedWithItsRole() {
        User admin = users.findByEmail(Email.of("admin@nexusmarket.com")).orElseThrow();

        assertInstanceOf(Administrator.class, admin);
        assertEquals(UserRole.ADMINISTRATOR, admin.getRole());
    }

    @Test
    void securityIsConfigured() {
        assertFalse(context.getBeansOfType(SecurityFilterChain.class).isEmpty());
    }
}
