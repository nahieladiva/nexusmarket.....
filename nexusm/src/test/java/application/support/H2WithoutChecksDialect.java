package application.support;

import org.hibernate.dialect.H2Dialect;

/**
 * H2 dialect without column checks, so the test schema is like the one in MySQL.
 *
 * <p>Hibernate adds a check to enum columns on H2, and H2 evaluates it with the session that
 * created the table. That session is already closed, so every insert would fail.</p>
 */
public class H2WithoutChecksDialect extends H2Dialect {

    public static final String PROPERTY =
        "spring.jpa.properties.hibernate.dialect=application.support.H2WithoutChecksDialect";

    @Override
    public boolean supportsColumnCheck() {
        return false;
    }
}
