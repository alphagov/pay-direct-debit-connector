package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateIdAndBankReference;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;

import java.time.LocalDate;
import java.util.Optional;

public interface DirectDebitPaymentProviderCommandService {

    PaymentProviderMandateIdAndBankReference confirmMandate(Mandate mandate, BankAccountDetails bankAccountDetails);

    LocalDate collect(Mandate mandate, Payment payment);

    BankAccountValidationResponse validate(Mandate mandate, BankAccountDetails bankAccountDetails);

    Optional<SunName> getSunName(Mandate mandate);

}
