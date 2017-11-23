package uk.gov.pay.directdebit.webhook.gocardless.exception;

public class InvalidWebhookException extends RuntimeException {

    public InvalidWebhookException(String message) {
        super(message);
    }

}
