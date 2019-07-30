package uk.gov.pay.directdebit.events.model;

import java.util.Optional;

/**
 * GoCardless mandate actions
 *
 * @see <a href="https://developer.gocardless.com/api-reference/#events-mandate-actions">https://developer.gocardless.com/api-reference/#events-mandate-actions</a>
 */
public enum GoCardlessMandateAction {
    CREATED, SUBMITTED, ACTIVE, FAILED, CANCELLED;

    public static Optional<GoCardlessMandateAction> fromString(String type) {
        try {
            return Optional.of(GoCardlessMandateAction.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
