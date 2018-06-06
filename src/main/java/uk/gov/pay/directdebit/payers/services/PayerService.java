package uk.gov.pay.directdebit.payers.services;

import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.exception.PayerNotFoundException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payers.api.PayerParser;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;

public class PayerService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(PayerService.class);

    private final PayerDao payerDao;
    private final MandateService mandateService;
    private final PayerParser payerParser;

    @Inject
    public PayerService(PayerDao payerDao,
                        MandateService mandateService,
                        PayerParser payerParser) {
        this.payerDao = payerDao;
        this.mandateService = mandateService;
        this.payerParser = payerParser;
    }

    public Payer getPayerFor(Transaction transaction) {
        return payerDao
                .findByTransactionId(transaction.getId())
                .orElseThrow(() -> new PayerNotFoundException(transaction.getExternalId()));
    }

    public Payer getPayerFor(Mandate mandate) {
        return payerDao
                .findByMandateId(mandate.getId())
                .orElseThrow(() -> new PayerNotFoundException(mandate.getExternalId()));
    }
    public Payer createOrUpdatePayer(String mandateExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest) {
        Mandate mandate = mandateService.receiveDirectDebitDetailsFor(mandateExternalId);
        Payer payerDetails = payerParser.parse(createPayerRequest, mandate.getId());

        return payerDao
                .findByMandateId(mandate.getId())
                .map(oldPayer -> editPayer(mandate, oldPayer, payerDetails))
                .orElseGet(() -> create(mandate, payerDetails));
    }
    
    private Payer editPayer(Mandate mandate, Payer oldPayer, Payer newPayer) {
        LOGGER.info("Updating payer with external id {}", oldPayer.getExternalId());
        payerDao.updatePayerDetails(oldPayer.getId(), newPayer);
        mandateService.payerEditedFor(mandate);
        return newPayer;
    }

    public Payer create(Mandate mandate, Payer payer) {
        Long id = payerDao.insert(payer);
        payer.setId(id);
        LOGGER.info("Created Payer with external id {}", payer.getExternalId());
        mandateService.payerCreatedFor(mandate);
        return payer;
    }
}
