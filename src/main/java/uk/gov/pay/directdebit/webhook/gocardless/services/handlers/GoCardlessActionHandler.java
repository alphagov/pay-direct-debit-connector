package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;

public interface GoCardlessActionHandler {
    void handle(GoCardlessEvent event);
}
