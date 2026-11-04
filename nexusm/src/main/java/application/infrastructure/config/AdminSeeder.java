package application.infrastructure.config;

import application.domain.models.Administrator;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.PasswordEncoderPort;
import application.domain.ports.out.UserRepository;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Crea el administrador inicial al arrancar, si no existe. Los administradores no se
 * registran por la API.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(UserRepository userRepository, PasswordEncoderPort passwordEncoder,
                       IdGeneratorPort idGenerator, ClockPort clock,
                       @Value("${nexusmarket.admin.email}") String adminEmail,
                       @Value("${nexusmarket.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        Email email = Email.of(adminEmail);
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }
        userRepository.save(new Administrator(UserId.of(idGenerator.newId()), "Administrador",
            email, PhoneNumber.of("+570000000000"), passwordEncoder.encode(adminPassword),
            List.of(), clock.now()));
        log.info("Administrador inicial creado: {}", email);
    }
}
