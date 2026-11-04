package application.domain.models;

import application.domain.enums.UserRole;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Marketplace administrator: registers sellers and internal staff, manages
 * marketplace warehouses, and approves returns and refunds.
 * The first administrator is not registered through the API; it is created at
 * startup with the configured credentials.
 */
public class Administrator extends User {

    public Administrator(UserId id, String fullName, Email email, PhoneNumber phone,
                         String passwordHash, List<Address> addresses, LocalDateTime createdAt) {
        super(id, fullName, email, phone, UserRole.ADMINISTRATOR, passwordHash, addresses, createdAt);
    }
}
