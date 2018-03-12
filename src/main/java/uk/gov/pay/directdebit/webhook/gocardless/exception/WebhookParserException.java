package uk.gov.pay.directdebit.webhook.gocardless.exception;

import uk.gov.pay.directdebit.common.exception.InternalServerErrorException;

public class WebhookParserException extends InternalServerErrorException {

    public WebhookParserException(String message) {
        super(message);
    }

}
