package application.adapters.out.persistence.mongodb.repositories;

import application.adapters.out.persistence.mongodb.documents.AuditLogDocument;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * MongoDB repository of audit documents.
 */
public interface AuditLogMongoRepository extends MongoRepository<AuditLogDocument, String> {
}