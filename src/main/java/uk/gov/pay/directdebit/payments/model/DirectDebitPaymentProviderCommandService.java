package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;

import java.time.LocalDate;
import java.util.Optional;

public interface DirectDebitPaymentProviderCommandService {

    Mandate confirmOnDemandMandate(Mandate mandate, BankAccountDetails bankAccountDetails);

    LocalDate collect(Mandate mandate, Transaction transaction);

    BankAccountValidationResponse validate(Mandate mandate, BankAccountDetails bankAccountDetails);

    Optional<SunName> getSunName(Mandate mandate);

}
