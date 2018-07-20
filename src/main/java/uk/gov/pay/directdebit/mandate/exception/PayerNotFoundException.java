package uk.gov.pay.directdebit.mandate.exception;


import uk.gov.pay.directdebit.common.exception.NotFoundException;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import static java.lang.String.format;

public class PayerNotFoundException extends NotFoundException {

    public PayerNotFoundException(MandateExternalId mandateExternalId) {
        super(format("Couldn't find payer for mandate with external id: %s", mandateExternalId));
    }
}
