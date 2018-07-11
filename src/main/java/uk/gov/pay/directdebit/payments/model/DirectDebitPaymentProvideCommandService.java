package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.mandate.model.MandateConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.OneOffMandateConfirmationDetails;

public interface DirectDebitPaymentProvideCommandService {
    void confirmMandate(MandateConfirmationDetails mandateConfirmationDetails);
    void confirmMandate(OneOffMandateConfirmationDetails mandateConfirmationDetails);

}
