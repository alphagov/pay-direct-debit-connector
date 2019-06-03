package uk.gov.pay.directdebit.payers.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.exception.PayerNotFoundException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdateService;
import uk.gov.pay.directdebit.payers.api.PayerParser;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.model.Payer;

import javax.inject.Inject;
import java.util.Map;

public class PayerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PayerService.class);

    private final PayerDao payerDao;
    private final MandateQueryService mandateQueryService;
    private final PayerParser payerParser;
    private final MandateStateUpdateService mandateStateUpdateService;

    @Inject
    public PayerService(PayerDao payerDao,
                        MandateQueryService mandateQueryService,
                        PayerParser payerParser, MandateStateUpdateService mandateStateUpdateService) {
        this.payerDao = payerDao;
        this.mandateQueryService = mandateQueryService;
        this.payerParser = payerParser;
        this.mandateStateUpdateService = mandateStateUpdateService;
    }
    
    public Payer getPayerFor(Mandate mandate) {
        return payerDao
                .findByMandateId(mandate.getId())
                .orElseThrow(() -> new PayerNotFoundException(mandate.getExternalId()));
    }
    public Payer createOrUpdatePayer(MandateExternalId mandateExternalId, Map<String, String> createPayerRequest) {
        Mandate mandate = mandateQueryService.findByExternalId(mandateExternalId);
        mandateStateUpdateService.receiveDirectDebitDetailsFor(mandate);
        Payer payerDetails = payerParser.parse(createPayerRequest, mandate.getId());

        return payerDao
                .findByMandateId(mandate.getId())
                .map(oldPayer -> editPayer(mandate, oldPayer, payerDetails))
                .orElseGet(() -> create(mandate, payerDetails));
    }
    
    private Payer editPayer(Mandate mandate, Payer oldPayer, Payer newPayer) {
        LOGGER.info("Updating payer with external id {}", oldPayer.getExternalId());
        payerDao.updatePayerDetails(oldPayer.getId(), newPayer);
        mandateStateUpdateService.payerEditedFor(mandate);
        return newPayer;
    }

    public Payer create(Mandate mandate, Payer payer) {
        Long id = payerDao.insert(payer);
        payer.setId(id);
        LOGGER.info("Created Payer with external id {}", payer.getExternalId());
        mandateStateUpdateService.payerCreatedFor(mandate);
        return payer;
    }

}
