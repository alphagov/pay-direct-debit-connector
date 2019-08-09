package uk.gov.pay.directdebit.mandate.exception;


import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.directdebit.common.exception.NotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateId;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import static java.lang.String.format;

public class MandateNotFoundException extends NotFoundException {

    public MandateNotFoundException(String mandateId) {
        super(format("Couldn't find mandate with id: %s", mandateId), ErrorIdentifier.MANDATE_ID_INVALID);
    }

    public MandateNotFoundException(MandateExternalId mandateExternalId) {
        super(format("Couldn't find mandate with id: %s", mandateExternalId), ErrorIdentifier.MANDATE_ID_INVALID);
    }

    public MandateNotFoundException(MandateExternalId mandateExternalId, String gatewayAccountExternalId) {
        super(format("Couldn't find mandate for gateway account %s with id: %s", gatewayAccountExternalId,
                mandateExternalId), 
                ErrorIdentifier.MANDATE_ID_INVALID);
    }

    public MandateNotFoundException(PaymentProviderMandateId paymentProviderMandateId, GoCardlessOrganisationId organisationId) {
        super(format("Couldn't find GoCardless mandate %s for organisation %s", paymentProviderMandateId,
                organisationId), 
                ErrorIdentifier.MANDATE_ID_INVALID);
    }
}
