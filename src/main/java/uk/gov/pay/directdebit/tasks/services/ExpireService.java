package uk.gov.pay.directdebit.tasks.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.mandate.services.MandateService;
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
    private static final Logger LOGGER = PayLoggerFactory.getLogger(ExpireService.class);
    private final long MIN_EXPIRY_AGE_MINUTES = 90l;
    private final PaymentState PAYMENT_EXPIRY_CUTOFF_STATUS = PaymentState.PENDING;
    private final MandateState MANDATE_EXPIRY_CUTOFF_STATUS = MandateState.PENDING;
    private MandateService mandateService;


    @Inject
    ExpireService(TransactionService transactionService, MandateStatesGraph mandateStatesGraph,
                         PaymentStatesGraph paymentStatesGraph, MandateService mandateService) {
        this.transactionService = transactionService;
        this.paymentStatesGraph = paymentStatesGraph;
        this.mandateService = mandateService;
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
            mandateService.mandateExpiredFor(mandate);
            LOGGER.info("Expired mandate " + mandate.getId());
        }
        return mandatesToExpire.size();
    }

    private List<Mandate> getMandatesForExpiration() {
        Set<MandateState> states = mandateStatesGraph.getPriorStates(MANDATE_EXPIRY_CUTOFF_STATUS);
        ZonedDateTime cutOffTime = ZonedDateTime.now().minusMinutes(MIN_EXPIRY_AGE_MINUTES);
        return mandateService.findAllMandatesBySetOfStatesAndMaxCreationTime(states, cutOffTime);
    }
}
