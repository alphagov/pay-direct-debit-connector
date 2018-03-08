package uk.gov.pay.directdebit.webhook.gocardless.services;

import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;

public interface GoCardlessActionHandler {
    void handle(GoCardlessEvent event);
}
