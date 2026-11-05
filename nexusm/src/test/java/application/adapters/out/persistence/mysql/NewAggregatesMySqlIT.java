package application.adapters.out.persistence.mysql;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

/**
 * Runs {@link NewAggregatesPersistenceTest} on the real MySQL. Each test rolls back its changes.
 *
 * <p>{@code mvnw test} does not run it. It needs {@code docker compose up -d mysql}, and then:
 * {@code mvnw test -Dtest=*IT}.</p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NewAggregatesMySqlIT extends NewAggregatesPersistenceTest {
}
