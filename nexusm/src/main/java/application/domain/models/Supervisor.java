package application.domain.models;

import application.domain.enums.UserRole;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Supervisor: consults all marketplace information and reports, but cannot
 * modify business data. Registered by an administrator.
 */
public class Supervisor extends User {

    public Supervisor(UserId id, String fullName, Email email, PhoneNumber phone,
                      String passwordHash, List<Address> addresses, LocalDateTime createdAt) {
        super(id, fullName, email, phone, UserRole.SUPERVISOR, passwordHash, addresses, createdAt);
    }

    /**
     * A supervisor never performs write operations on business data.
     */
    public boolean canModifyBusinessData() {
        return false;
    }
}
