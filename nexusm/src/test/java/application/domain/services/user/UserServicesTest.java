package application.domain.services.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.SellerStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.models.Buyer;
import application.domain.models.LogisticsOperator;
import application.domain.models.Seller;
import application.domain.models.Supervisor;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the user use cases.
 */
class UserServicesTest {

    private static final PhoneNumber PHONE = PhoneNumber.of("+573001112233");

    private TestFixture f;
    private User admin;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        admin = f.admin();
    }

    private Seller registerSeller(String email) {
        return (Seller) f.registerSeller.execute(admin, "Seller", Email.of(email), PHONE, "Password2026", "Store");
    }

    private Address address(int number) {
        return Address.of("Street " + number, "City", "State", "050001", "Colombia");
    }

    @Test
    void buyerRegistersPubliclyAndEmailsAreUnique() {
        User buyer = f.registerBuyer.execute("Ana", Email.of("ana@mail.com"), PHONE, "Password2026",
            TestFixture.address());
        assertInstanceOf(Buyer.class, buyer);
        assertThrows(DuplicateResourceException.class, () -> f.registerBuyer.execute("Ana",
            Email.of("ANA@mail.com"), PHONE, "Password2026", TestFixture.address()));
    }

    @Test
    void passwordMustFollowThePolicy() {
        assertThrows(BusinessRuleException.class, () -> f.registerBuyer.execute("Ana",
            Email.of("ana@mail.com"), PHONE, "short", TestFixture.address()));
    }

    @Test
    void onlyAnAdministratorRegistersSellersAndStaff() {
        User seller = registerSeller("seller@mail.com");
        assertThrows(ForbiddenActionException.class, () -> f.registerSeller.execute(seller, "Other",
            Email.of("other@mail.com"), PHONE, "Password2026", "Store"));
        assertThrows(ForbiddenActionException.class, () -> f.registerStaffUser.execute(seller,
            UserRole.SUPERVISOR, "Other", Email.of("other@mail.com"), PHONE, "Password2026"));
    }

    @Test
    void staffUserGetsTheSubclassOfItsRole() {
        assertInstanceOf(LogisticsOperator.class, f.registerStaffUser.execute(admin,
            UserRole.LOGISTICS_OPERATOR, "Op", Email.of("op@mail.com"), PHONE, "Password2026"));
        assertInstanceOf(Supervisor.class, f.registerStaffUser.execute(admin, UserRole.SUPERVISOR,
            "Sup", Email.of("sup@mail.com"), PHONE, "Password2026"));
        assertThrows(BusinessRuleException.class, () -> f.registerStaffUser.execute(admin, UserRole.BUYER,
            "Buyer", Email.of("buyer@mail.com"), PHONE, "Password2026"));
    }

    @Test
    void sellerLifecycle() {
        Seller seller = registerSeller("seller@mail.com");
        assertEquals(SellerStatus.PENDING_APPROVAL, seller.getStatus());
        assertEquals(SellerStatus.APPROVED, f.approveSeller.execute(admin, seller.getId()).getStatus());
        assertThrows(BusinessRuleException.class, () -> f.approveSeller.execute(admin, seller.getId()));
        assertEquals(SellerStatus.SUSPENDED, f.suspendSeller.execute(admin, seller.getId()).getStatus());
        assertEquals(SellerStatus.APPROVED, f.approveSeller.execute(admin, seller.getId()).getStatus());
    }

    @Test
    void onlySellersAreApproved() {
        Buyer buyer = f.buyer("buyer@mail.com");
        assertThrows(BusinessRuleException.class, () -> f.approveSeller.execute(admin, buyer.getId()));
    }

    @Test
    void profileAndAddressesAreManagedByTheUser() {
        Buyer buyer = f.buyer("buyer@mail.com");
        assertEquals("New name", f.updateProfile.execute(buyer, "New name", null).getFullName());
        f.addAddress.execute(buyer, address(1));
        assertThrows(DuplicateResourceException.class, () -> f.addAddress.execute(buyer, address(1)));
        assertTrue(f.removeAddress.execute(buyer, address(1)).getAddresses().isEmpty());
    }

    @Test
    void addressesHaveALimit() {
        Buyer buyer = f.buyer("buyer@mail.com");
        for (int i = 0; i < AddAddressService.MAX_ADDRESSES; i++) {
            f.addAddress.execute(buyer, address(i));
        }
        assertThrows(BusinessRuleException.class, () -> f.addAddress.execute(buyer, address(99)));
    }

    @Test
    void usersAreVisibleToAdministratorsAndSupervisors() {
        Buyer buyer = f.buyer("buyer@mail.com");
        assertEquals(buyer.getId(), f.consultUser.execute(admin, buyer.getId()).getId());
        assertEquals(2, f.consultUser.findAll(admin).size());
        assertThrows(ForbiddenActionException.class, () -> f.consultUser.findAll(buyer));
    }
}
