package application.domain.services;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a domain service (one business use case).
 *
 * <p>It is plain Java, so the domain does not depend on Spring. The infrastructure scans this
 * annotation and registers each service as a bean.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DomainService {
}
