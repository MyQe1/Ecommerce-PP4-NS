package pl.mbobowski.sales;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.mbobowski.sales.cart.CartStorage;
import pl.mbobowski.sales.offerting.Offer;
import pl.mbobowski.sales.offerting.OfferCalculator;
import pl.mbobowski.sales.payment.DummyPaymentGateway;
import pl.mbobowski.sales.payment.PaymentDetails;
import pl.mbobowski.sales.product.ListProductDetailsProvider;
import pl.mbobowski.sales.product.ProductDetails;
import pl.mbobowski.sales.reservation.InMemoryReservationStorage;
import pl.mbobowski.sales.reservation.Reservation;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderingTest {

    List<ProductDetails> availableProducts;
    InMemoryReservationStorage reservationStorage;

    @BeforeEach
    void setup() {

        this.availableProducts = new ArrayList<>();
        this.reservationStorage = new InMemoryReservationStorage();
    }

    @Test
    void acceptingOfferHappyPath() {
        //Arrange // Given
        String customerId = thereIsClient();
        Sales sales = thereIsSalesModule();
        String productId = thereIsProduct("lego", BigDecimal.valueOf(10.10));
        sales.addToCart(customerId, productId);
        Offer offer = sales.getCurrentOffer(customerId);

        //Act // When
        PaymentDetails paymentDetails = sales.acceptOffer(customerId, exampleCustomerData());

        //Assert // Then // Expected
        assertNotNull(paymentDetails);
        assertNotNull(paymentDetails.getUrl());
        assertNotNull(paymentDetails.getReservationId());
        thereReservationWithIdExistsWithinTheSystem(paymentDetails.getReservationId());
        totalOfreservationWithIdEquals(paymentDetails.getReservationId(), BigDecimal.valueOf(10.10));
    }

    private void totalOfreservationWithIdEquals(String reservationId, BigDecimal totalAmount) {
        Reservation reservation = reservationStorage.findById(reservationId).get();

        assertEquals(totalAmount, reservation.getTotal());
    }

    private void thereReservationWithIdExistsWithinTheSystem(String reservationId) {
        Optional<Reservation> optionalReservation = reservationStorage.findById(reservationId);
        assertTrue(optionalReservation.isPresent());
    }

    private CustomerData exampleCustomerData() {
        return new CustomerData("John", "doe", "john.doe@example.com");
    }

    private String thereIsClient() {
        return "kuba";
    }

    private String thereIsProduct(String productId, BigDecimal price) {
        ProductDetails productDetails = new ProductDetails(productId, "Some name", price);
        availableProducts.add(productDetails);

        return productId;
    }

    private Sales thereIsSalesModule() {
        return new Sales(
                new CartStorage(),
                new ListProductDetailsProvider(availableProducts),
                new DummyPaymentGateway(),
                reservationStorage,
                new OfferCalculator()
        );
    }
}
