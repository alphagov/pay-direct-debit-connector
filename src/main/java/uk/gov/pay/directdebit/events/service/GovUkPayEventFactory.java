package uk.gov.pay.directdebit.events.service;

import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import java.time.ZonedDateTime;

public class GovUkPayEventFactory {
    
    public GovUkPayEvent createCancelEvent(MandateExternalId mandateExternalId) {
        return new GovUkPayEvent(RandomIdGenerator.newId(), "CANCELLED", ZonedDateTime.now(), mandateExternalId);
    }
}
