package application.adapters.out.persistence.mongodb.documents;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document of an issued token (the "JWT database").
 * {@code expiresAt} is a {@link Date} so the TTL index removes it when it expires.
 * {@code status} stores the name of {@link application.domain.enums.TokenStatus}.
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
    private String status;
}
