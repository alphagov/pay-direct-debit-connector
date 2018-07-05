package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.InternalServerErrorException;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import static java.lang.String.format;

public class CreateCustomerBankAccountFailedException extends InternalServerErrorException {

    public CreateCustomerBankAccountFailedException(MandateExternalId mandateExternalId, String payerId) {
        super(format("Failed to create customer bank account in gocardless, mandate id: %s, payer id: %s", mandateExternalId, payerId));
    }
}
