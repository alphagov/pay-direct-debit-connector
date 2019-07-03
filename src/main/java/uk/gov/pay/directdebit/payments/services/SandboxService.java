package uk.gov.pay.directdebit.payments.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateIdAndBankReference;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProviderCommandService;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentIdAndChargeDate;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;

public class SandboxService implements DirectDebitPaymentProvider, DirectDebitPaymentProviderCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SandboxService.class);
    private static final int DAYS_TO_COLLECTION = 4;

    private final PaymentDao paymentDao;

    @Inject
    public SandboxService(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    @Override
    public PaymentProviderMandateIdAndBankReference confirmMandate(Mandate mandate, BankAccountDetails bankAccountDetails) {
        LOGGER.info("Confirming on demand mandate for sandbox, mandate with id: {}", mandate.getExternalId());
        return new PaymentProviderMandateIdAndBankReference(
                SandboxMandateId.valueOf(mandate.getExternalId().toString()),
                MandateBankStatementReference.valueOf(RandomStringUtils.randomAlphanumeric(18)));
    }

    @Override
    public PaymentProviderPaymentIdAndChargeDate collect(Mandate mandate, Payment payment) {
        LOGGER.info("Collecting payment for sandbox, mandate with id: {}, payment with id: {}", mandate.getExternalId(), payment.getExternalId());
        var providerIdAndChargeDate = new PaymentProviderPaymentIdAndChargeDate(
                SandboxPaymentId.valueOf(payment.getExternalId()),
                LocalDate.now().plusDays(DAYS_TO_COLLECTION));

        Payment updatePayment = Payment.PaymentBuilder.fromPayment(payment)
                .withProviderId(providerIdAndChargeDate.getPaymentProviderPaymentId())
                .withChargeDate(providerIdAndChargeDate.getChargeDate())
                .build();

        paymentDao.updateProviderIdAndChargeDate(updatePayment);
        return providerIdAndChargeDate;
    }

    @Override
    public BankAccountValidationResponse validate(Mandate mandate, BankAccountDetails bankAccountDetails) {
        LOGGER.info("Validating bank account details for SANDBOX, mandate with id: {}", mandate.getExternalId());
        return new BankAccountValidationResponse(true, "Sandbox Bank");
    }

    @Override
    public Optional<SunName> getSunName(Mandate mandate) {
        return Optional.of(SunName.of("Sandbox SUN Name"));
    }

}
