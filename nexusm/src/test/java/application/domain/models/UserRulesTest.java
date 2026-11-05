package application.domain.models;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.SellerStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceStateConflictException;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import application.support.TestFixture;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Rules of the users and of their roles.
 */
class UserRulesTest {

    private static final PhoneNumber PHONE = PhoneNumber.of("+573001234567");
    private static final String HASH = "hashed:Password123";
    private static final Address ADDRESS = TestFixture.address();

    private static Seller seller(SellerStatus status) {
        return new Seller(UserId.random(), "Seller", Email.of("seller@mail.com"), PHONE, HASH, "Store",
            List.of(), status, TestFixture.NOW);
    }

    private static Administrator administrator() {
        return new Administrator(UserId.random(), "Admin", Email.of("admin@mail.com"), PHONE, HASH,
            List.of(), TestFixture.NOW);
    }

    private static Buyer buyer() {
        return new Buyer(UserId.random(), "Buyer", Email.of("buyer@mail.com"), PHONE, HASH, ADDRESS,
            List.of(), TestFixture.NOW);
    }

    @Test
    void eachUserAnswersForItsOwnRole() {
        assertTrue(buyer().isBuyer());
        assertTrue(seller(SellerStatus.APPROVED).isSeller());
        assertTrue(administrator().isAdministrator());
        assertTrue(new LogisticsOperator(UserId.random(), "Op", Email.of("op@mail.com"), PHONE, HASH,
            List.of(), TestFixture.NOW).isLogisticsOperator());
        assertTrue(new Supervisor(UserId.random(), "Sup", Email.of("sup@mail.com"), PHONE, HASH,
            List.of(), TestFixture.NOW).isSupervisor());
        assertFalse(buyer().isAdministrator());
        assertTrue(buyer().hasRole(UserRole.BUYER));
        assertFalse(buyer().hasRole(UserRole.SELLER));
    }

    @Test
    void aRoleCanBeRequired() {
        assertDoesNotThrow(() -> buyer().requireRole(UserRole.BUYER));
        assertThrows(ForbiddenActionException.class, () -> buyer().requireRole(UserRole.SELLER));
        assertDoesNotThrow(() -> buyer().requireAnyRole(UserRole.SELLER, UserRole.BUYER));
        assertThrows(ForbiddenActionException.class,
            () -> buyer().requireAnyRole(UserRole.SELLER, UserRole.SUPERVISOR));
    }

    @Test
    void aNameCannotBeBlank() {
        Buyer buyer = buyer();

        assertThrows(IllegalArgumentException.class, () -> buyer.changeName(" "));
        assertThrows(IllegalArgumentException.class, () -> buyer.changeName(null));
        buyer.changeName("  New Name ");
        assertEquals("New Name", buyer.getFullName());
    }

    @Test
    void aPhoneAndAPasswordCanChange() {
        Buyer buyer = buyer();

        buyer.changePhone(PhoneNumber.of("+573009998877"));
        buyer.changePasswordHash("hashed:Other");

        assertEquals("+573009998877", buyer.getPhone().getValue());
        assertEquals("hashed:Other", buyer.getPasswordHash());
    }

    @Test
    void anAddressMustBeRegisteredToBeRemoved() {
        Buyer buyer = buyer();
        buyer.addAddress(ADDRESS);

        assertTrue(buyer.hasAddress(ADDRESS));
        buyer.removeAddress(ADDRESS);
        assertFalse(buyer.hasAddress(ADDRESS));
        assertThrows(IllegalArgumentException.class, () -> buyer.removeAddress(ADDRESS));
    }

    @Test
    void aBuyerKeepsItsShippingAddress() {
        assertEquals(ADDRESS, buyer().getDefaultShippingAddress());
    }

    @Test
    void aSellerStartsPendingApproval() {
        Seller seller = Seller.register(UserId.random(), "Seller", Email.of("new@mail.com"), PHONE, HASH,
            "Store", TestFixture.NOW);

        assertEquals(SellerStatus.PENDING_APPROVAL, seller.getStatus());
        assertFalse(seller.isApproved());
    }

    @Test
    void onlyAnApprovedSellerCanPublish() {
        assertDoesNotThrow(() -> seller(SellerStatus.APPROVED).requireApprovedToPublish());
        assertThrows(ResourceStateConflictException.class,
            () -> seller(SellerStatus.PENDING_APPROVAL).requireApprovedToPublish());
        assertThrows(ResourceStateConflictException.class,
            () -> seller(SellerStatus.SUSPENDED).requireApprovedToPublish());
    }

    @Test
    void anApprovedSellerCanBeSuspendedAndApprovedAgain() {
        Seller seller = seller(SellerStatus.APPROVED);

        seller.suspend();
        assertEquals(SellerStatus.SUSPENDED, seller.getStatus());
        seller.approve();
        assertTrue(seller.isApproved());
    }

    @Test
    void approvingAnApprovedSellerIsAConflict() {
        Seller seller = seller(SellerStatus.APPROVED);

        assertThrows(BusinessRuleException.class, seller::approve);
    }

    @Test
    void aBusinessNameCanChangeButNotBeBlank() {
        Seller seller = seller(SellerStatus.APPROVED);

        seller.changeBusinessName("New Store");

        assertEquals("New Store", seller.getBusinessName());
        assertThrows(IllegalArgumentException.class, () -> seller.changeBusinessName(" "));
    }
}
