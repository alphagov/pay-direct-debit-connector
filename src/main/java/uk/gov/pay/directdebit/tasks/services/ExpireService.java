package uk.gov.pay.directdebit.tasks.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
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
    private static final Logger LOGGER = PayLoggerFactory.getLogger(ExpireService.class);
    private final long MIN_EXPIRY_AGE_MINUTES = 90l;
    private final PaymentState PAYMENT_EXPIRY_CUTOFF_STATUS = PaymentState.PENDING;
    
    @Inject
    public ExpireService(TransactionService transactionService, PaymentStatesGraph paymentStatesGraph) {
        this.transactionService = transactionService;
        this.paymentStatesGraph = paymentStatesGraph;
    }
    
    public int expirePayments() {
        LOGGER.info("Starting expire payments process.");
        List<Transaction> paymentsToExpire = getPaymentsForExpiration();
        int expirePaymentsCounter = 0;
        for (Transaction payment : paymentsToExpire) {
            transactionService.paymentExpired(payment);
            LOGGER.info("Expired payment " + payment.getId());
            expirePaymentsCounter++;
        }
        return expirePaymentsCounter;
    }

    private List<Transaction> getPaymentsForExpiration() {
        Set<PaymentState> states = paymentStatesGraph.getPriorStates(PAYMENT_EXPIRY_CUTOFF_STATUS);
        ZonedDateTime cutOffTime = ZonedDateTime.now().minusMinutes(MIN_EXPIRY_AGE_MINUTES);
        return transactionService.findAllPaymentsBySetOfStatesAndCreationTime(states, cutOffTime);
    }
}
