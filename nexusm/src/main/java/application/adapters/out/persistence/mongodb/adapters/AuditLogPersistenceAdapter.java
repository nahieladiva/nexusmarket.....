package application.adapters.out.persistence.mongodb.adapters;

import application.adapters.out.persistence.mongodb.mappers.AuditLogMapper;
import application.adapters.out.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.domain.events.DomainEvent;
import application.domain.ports.out.AuditLogPort;

import java.util.Objects;

import org.springframework.stereotype.Component;

/**
 * Output adapter that implements {@link AuditLogPort} on MongoDB.
 */
@Component
public class AuditLogPersistenceAdapter implements AuditLogPort {

    private final AuditLogMongoRepository repository;
    private final AuditLogMapper mapper;

    public AuditLogPersistenceAdapter(AuditLogMongoRepository repository,
                                      AuditLogMapper mapper) {
        this.repository = Objects.requireNonNull(repository, "repository is required");
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
    }

    @Override
    public void record(DomainEvent event) {
        repository.save(mapper.toDocument(event));
    }
}