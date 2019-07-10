package uk.gov.pay.directdebit.mandate.exception;


import uk.gov.pay.directdebit.common.exception.NotFoundException;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payments.model.GoCardlessMandateIdAndOrganisationId;

import static java.lang.String.format;

public class MandateNotFoundException extends NotFoundException {

    public MandateNotFoundException(String mandateId) {
        super(format("Couldn't find mandate with id: %s", mandateId));
    }

    public MandateNotFoundException(MandateExternalId mandateExternalId) {
        super(format("Couldn't find mandate with id: %s", mandateExternalId));
    }

    public MandateNotFoundException(MandateExternalId mandateExternalId, String gatewayAccountExternalId) {
        super(format("Couldn't find mandate for gateway account %s with id: %s", gatewayAccountExternalId, mandateExternalId));
    }

    public MandateNotFoundException(GoCardlessMandateIdAndOrganisationId goCardlessMandateIdAndOrganisationId) {
        super(format("Couldn't find GoCardless mandate %s for organisation %s", goCardlessMandateIdAndOrganisationId.getGoCardlessMandateId(),
                goCardlessMandateIdAndOrganisationId.getGoCardlessOrganisationId()));
    }
}
