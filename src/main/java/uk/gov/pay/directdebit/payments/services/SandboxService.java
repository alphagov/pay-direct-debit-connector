package uk.gov.pay.directdebit.payments.services;

import java.time.LocalDate;
import javax.inject.Inject;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.OneOffConfirmationDetails;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProviderCommandService;
import uk.gov.pay.directdebit.payments.model.Transaction;

public class SandboxService implements DirectDebitPaymentProvider,
        DirectDebitPaymentProviderCommandService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(SandboxService.class);
    private static final int DAYS_TO_COLLECTION = 4;

    @Inject
    public SandboxService() { }

    @Override
    public OneOffConfirmationDetails confirmOneOffMandate(Mandate mandate, BankAccountDetails bankAccountDetails, Transaction transaction) {
        LOGGER.info("Confirming one off mandate for sandbox, mandate with id: {}", mandate.getExternalId());
        return new OneOffConfirmationDetails(mandate, LocalDate.now().plusDays(DAYS_TO_COLLECTION));
    }

    @Override
    public Mandate confirmOnDemandMandate(Mandate mandate, BankAccountDetails bankAccountDetails) {
        LOGGER.info("Confirming on demand mandate for sandbox, mandate with id: {}", mandate.getExternalId());
        return mandate;
    }

    @Override
    public LocalDate collect(Mandate mandate, Transaction transaction) {
        LOGGER.info("Collecting payment for sandbox, mandate with id: {}, transaction with id: {}", mandate.getExternalId(), transaction.getExternalId());
        return LocalDate.now().plusDays(DAYS_TO_COLLECTION);
    }

    @Override
    public BankAccountValidationResponse validate(Mandate mandate, BankAccountDetails bankAccountDetails) {
        LOGGER.info("Validating bank account details for SANDBOX, mandate with id: {}", mandate.getExternalId());
        return new BankAccountValidationResponse(true, "Sandbox Bank");
    }
}
