package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateId;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateIdAndBankReference;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;

import java.util.Optional;

public interface DirectDebitPaymentProviderCommandService<T extends PaymentProviderMandateId> {

    PaymentProviderMandateIdAndBankReference confirmMandate(Mandate mandate, BankAccountDetails bankAccountDetails);

    PaymentProviderPaymentIdAndChargeDate collect(Payment payment, T paymentProviderMandateId);

    BankAccountValidationResponse validate(Mandate mandate, BankAccountDetails bankAccountDetails);

    Optional<SunName> getSunName(Mandate mandate);

}
