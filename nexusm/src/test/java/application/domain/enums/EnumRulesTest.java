package application.domain.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ResourceStateConflictException;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Rules kept in the enums.
 */
class EnumRulesTest {

    @Test
    void onlyBuyersRegisterThemselves() {
        for (UserRole role : UserRole.values()) {
            assertEquals(role == UserRole.BUYER, role.canSelfRegister(), role.name());
        }
    }

    @Test
    void internalStaffIsCreatedByAnAdministrator() {
        Set<UserRole> staff = EnumSet.of(UserRole.LOGISTICS_OPERATOR, UserRole.ADMINISTRATOR,
            UserRole.SUPERVISOR);
        for (UserRole role : UserRole.values()) {
            assertEquals(staff.contains(role), role.isInternalStaff(), role.name());
        }
    }

    @Test
    void onlyTheSupervisorIsReadOnly() {
        for (UserRole role : UserRole.values()) {
            assertEquals(role == UserRole.SUPERVISOR, role.isReadOnly(), role.name());
        }
    }

    @Test
    void aRoleCanBeReadFromText() {
        assertEquals(UserRole.BUYER, UserRole.fromString(" buyer "));
        assertThrows(IllegalArgumentException.class, () -> UserRole.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> UserRole.fromString(" "));
        assertThrows(IllegalArgumentException.class, () -> UserRole.fromString("KING"));
    }

    @Test
    void anOrderStatusCanBeReadFromText() {
        assertEquals(OrderStatus.PAID, OrderStatus.fromString(" paid "));
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromString("LOST"));
    }

    @Test
    void anOrderCanBeCancelledUntilItShips() {
        assertTrue(OrderStatus.PENDING_PAYMENT.isCancellable());
        assertTrue(OrderStatus.PAID.isCancellable());
        assertFalse(OrderStatus.SHIPPED.isCancellable());
        assertFalse(OrderStatus.DELIVERED.isCancellable());
        assertFalse(OrderStatus.CANCELLED.isCancellable());
    }

    @Test
    void onlyAPaidOrderKeepsStockReserved() {
        for (OrderStatus status : OrderStatus.values()) {
            assertEquals(status == OrderStatus.PAID, status.keepsStockReserved(), status.name());
        }
    }

    @Test
    void onlyADeliveredOrderAllowsReturns() {
        for (OrderStatus status : OrderStatus.values()) {
            assertEquals(status == OrderStatus.DELIVERED, status.allowsReturns(), status.name());
        }
    }

    @Test
    void deliveredAndCancelledAreTerminal() {
        Set<OrderStatus> terminal = EnumSet.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED);
        for (OrderStatus status : OrderStatus.values()) {
            assertEquals(terminal.contains(status), status.isTerminal(), status.name());
        }
    }

    @Test
    void anInvalidStatusChangeIsAStateConflict() {
        assertThrows(ResourceStateConflictException.class,
            () -> RefundStatus.PROCESSED.requireCanTransitionTo(RefundStatus.REJECTED));
        assertThrows(ResourceStateConflictException.class,
            () -> ShipmentStatus.DELIVERED.requireCanTransitionTo(ShipmentStatus.PACKED));
        assertThrows(ResourceStateConflictException.class,
            () -> ReturnStatus.REJECTED.requireCanTransitionTo(ReturnStatus.APPROVED));
    }

    @Test
    void aStateConflictIsAlsoABusinessRule() {
        assertTrue(BusinessRuleException.class.isAssignableFrom(ResourceStateConflictException.class));
    }
}
