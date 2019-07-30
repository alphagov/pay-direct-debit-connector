package uk.gov.pay.directdebit.events.model;

import java.util.Optional;

/**
 * GoCardless payment actions
 *
 * @see <a href="https://developer.gocardless.com/api-reference/#events-payment-actions">https://developer.gocardless.com/api-reference/#events-payment-actions</a>
 */
public enum GoCardlessPaymentAction {
    CREATED, SUBMITTED, CONFIRMED, FAILED, PAID_OUT, PAID;

    public static Optional<GoCardlessPaymentAction> fromString(String type) {
        try {
            return Optional.of(GoCardlessPaymentAction.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
