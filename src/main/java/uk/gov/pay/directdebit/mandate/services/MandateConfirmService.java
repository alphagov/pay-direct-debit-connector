package uk.gov.pay.directdebit.mandate.services;

import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.model.Transaction;

import static java.lang.String.format;

public class MandateConfirmService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MandateConfirmService.class);
    private final MandateService mandateService;
    private final TransactionDao transactionDao;

    @Inject
    public MandateConfirmService(MandateService mandateService,
            TransactionDao transactionDao) {
        this.mandateService = mandateService;
        this.transactionDao = transactionDao;
    }

    /**
     * Creates a mandate and updates the mandate to a pending (Sandbox)
     *
     * @param mandateExternalId
     */
    public ConfirmationDetails confirm(String mandateExternalId, Map<String, String> confirmDetailsRequest) {
        String sortCode = confirmDetailsRequest.get("sort_code");
        String accountNumber = confirmDetailsRequest.get("account_number");
        Mandate mandate = mandateService.confirmedDirectDebitDetailsFor(mandateExternalId);
        Transaction transaction = Optional
                .ofNullable(confirmDetailsRequest.get("transaction_external_id"))
                .map(transactionExternalId -> transactionDao.findByExternalId(transactionExternalId)
                        .orElseThrow(() -> new ChargeNotFoundException(format("No charges found for mandate id %s, transaction id %s", 
                                mandateExternalId, transactionExternalId))
                ))
                .orElse(null);
        return new ConfirmationDetails(mandate, transaction, accountNumber, sortCode);
    }
}
