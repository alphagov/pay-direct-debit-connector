package uk.gov.pay.directdebit.tasks.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdateService;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentStatesGraph;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public class ExpireService {

    private TransactionService transactionService;
    private PaymentStatesGraph paymentStatesGraph;
    private MandateStatesGraph mandateStatesGraph;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpireService.class);
    private final long MIN_EXPIRY_AGE_MINUTES = 90L;
    private final PaymentState PAYMENT_EXPIRY_CUTOFF_STATUS = PaymentState.PENDING;
    private final MandateState MANDATE_EXPIRY_CUTOFF_STATUS = MandateState.PENDING;
    private final MandateQueryService mandateQueryService;
    private final MandateStateUpdateService mandateStateUpdateService;

    @Inject
    ExpireService(TransactionService transactionService, 
                  MandateStatesGraph mandateStatesGraph,
                  PaymentStatesGraph paymentStatesGraph, 
                  MandateQueryService mandateQueryService, 
                  MandateStateUpdateService mandateStateUpdateService) {
        this.transactionService = transactionService;
        this.paymentStatesGraph = paymentStatesGraph;
        this.mandateQueryService = mandateQueryService;
        this.mandateStateUpdateService = mandateStateUpdateService;
        this.mandateStatesGraph = mandateStatesGraph;
    }

    public int expirePayments() {
        LOGGER.info("Starting expire payments process.");
        List<Transaction> paymentsToExpire = getPaymentsForExpiration();
        for (Transaction payment : paymentsToExpire) {
            transactionService.paymentExpired(payment);
            LOGGER.info("Expired payment " + payment.getId());
        }
        return paymentsToExpire.size();
    }

    private List<Transaction> getPaymentsForExpiration() {
        Set<PaymentState> states = paymentStatesGraph.getPriorStates(PAYMENT_EXPIRY_CUTOFF_STATUS);
        ZonedDateTime cutOffTime = ZonedDateTime.now().minusMinutes(MIN_EXPIRY_AGE_MINUTES);
        return transactionService.findAllPaymentsBySetOfStatesAndCreationTime(states, cutOffTime);
    }

    public int expireMandates() {
        LOGGER.info("Starting expire mandates process.");
        List<Mandate> mandatesToExpire = getMandatesForExpiration();
        for (Mandate mandate : mandatesToExpire) {
            mandateStateUpdateService.mandateExpiredFor(mandate);
            LOGGER.info("Expired mandate " + mandate.getId());
        }
        return mandatesToExpire.size();
    }

    private List<Mandate> getMandatesForExpiration() {
        Set<MandateState> states = mandateStatesGraph.getPriorStates(MANDATE_EXPIRY_CUTOFF_STATUS);
        ZonedDateTime cutOffTime = ZonedDateTime.now().minusMinutes(MIN_EXPIRY_AGE_MINUTES);
        return mandateQueryService.findAllMandatesBySetOfStatesAndMaxCreationTime(states, cutOffTime);
    }
}
