package application.domain.models;

import application.domain.enums.UserRole;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Logistics operator: registers and adjusts inventory in warehouses, and creates,
 * dispatches and delivers shipments. Registered by an administrator.
 */
public class LogisticsOperator extends User {

    public LogisticsOperator(UserId id, String fullName, Email email, PhoneNumber phone,
                             String passwordHash, List<Address> addresses, LocalDateTime createdAt) {
        super(id, fullName, email, phone, UserRole.LOGISTICS_OPERATOR, passwordHash, addresses, createdAt);
    }
}
