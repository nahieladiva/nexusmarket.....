package application.adapters.out.persistence.mongodb.documents;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento MongoDB de un token emitido (la "base de datos de JWT").
 * {@code expiresAt} es {@link Date} para que el índice TTL lo borre al expirar.
 */
@Document(collection = IssuedTokenDocument.COLLECTION)
@Getter
@Setter
@NoArgsConstructor
public class IssuedTokenDocument {

    public static final String COLLECTION = "issued_tokens";

    @Id
    private String tokenId;
    private String userId;
    private LocalDateTime issuedAt;
    private Date expiresAt;
    private boolean revoked;
}
