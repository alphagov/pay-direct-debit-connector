package uk.gov.pay.directdebit.payments.model;

import java.time.LocalDate;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.OneOffConfirmationDetails;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;

public interface DirectDebitPaymentProviderCommandService {

    OneOffConfirmationDetails confirmOneOffMandate(Mandate mandate, BankAccountDetails bankAccountDetails, Transaction transaction);

    Mandate confirmOnDemandMandate(Mandate mandate, BankAccountDetails bankAccountDetails);

    LocalDate collect(Mandate mandate, Transaction transaction);

    BankAccountValidationResponse validate(Mandate mandate, BankAccountDetails bankAccountDetails);

}
