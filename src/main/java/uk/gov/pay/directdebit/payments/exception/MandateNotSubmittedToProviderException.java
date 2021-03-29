package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

public class MandateNotSubmittedToProviderException extends NotFoundException {

    public MandateNotSubmittedToProviderException(MandateExternalId mandateExternalId) {
        super("Mandate with external ID " + mandateExternalId.toString() + " has not been submitted to provider",
                ErrorIdentifier.GENERIC);
    }

}
