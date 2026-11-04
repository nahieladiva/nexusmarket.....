package application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.models.PriceQuote;
import application.domain.models.Product;
import application.domain.models.Promotion;
import application.domain.models.Seller;
import application.domain.valueobjects.DateRange;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.Percentage;
import application.support.TestFixture;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PromotionApplicationServiceTest {

    private static final LocalDate TODAY = TestFixture.NOW.toLocalDate();

    private TestFixture f;
    private Seller seller;
    private Product product;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        seller = f.approvedSeller("vende@tienda.com");
        product = f.product(seller, "TEC-0001", "200000");
    }

    private Promotion create(int pct, LocalDate start, LocalDate end) {
        return f.promotionService.createPromotion(seller.getId(), product.getId(),
            Percentage.of(pct), DateRange.of(start, end));
    }

    @Test
    void discountMustBeBetweenOneAndSeventy() {
        assertThrows(BusinessRuleException.class, () -> create(71, TODAY, TODAY.plusDays(2)));
        assertThrows(BusinessRuleException.class, () -> create(0, TODAY, TODAY.plusDays(2)));
    }

    @Test
    void endMustBeAfterStartAndNotInThePast() {
        assertThrows(BusinessRuleException.class, () -> create(10, TODAY, TODAY));
        assertThrows(BusinessRuleException.class, () -> create(10, TODAY.minusDays(1), TODAY.plusDays(2)));
    }

    @Test
    void onlyOnePromotionAtATimePerProduct() {
        create(10, TODAY, TODAY.plusDays(5));
        assertThrows(BusinessRuleException.class, () -> create(15, TODAY.plusDays(3), TODAY.plusDays(9)));
        create(15, TODAY.plusDays(6), TODAY.plusDays(9));
    }

    @Test
    void finalPriceUsesEffectivePromotion() {
        assertFalse(f.promotionService.calculateFinalPrice(product.getId()).hasDiscount());
        create(25, TODAY, TODAY.plusDays(5));
        PriceQuote quote = f.promotionService.calculateFinalPrice(product.getId());
        assertEquals(Money.of("150000", "COP"), quote.finalPrice());
        assertEquals(1, f.promotionService.findActivePromotions().size());
    }

    @Test
    void endedPromotionStopsApplyingAndOnlyCreatorCanEndIt() {
        Promotion promotion = create(25, TODAY, TODAY.plusDays(5));
        Seller other = f.approvedSeller("otro@tienda.com");
        assertThrows(ForbiddenActionException.class,
            () -> f.promotionService.endPromotion(other.getId(), promotion.getId()));
        f.promotionService.endPromotion(seller.getId(), promotion.getId());
        assertTrue(f.promotionService.findActivePromotions().isEmpty());
        assertEquals(Money.of("200000", "COP"),
            f.promotionService.calculateFinalPrice(product.getId()).finalPrice());
    }
}
