package uk.gov.pay.directdebit.tasks.services;

import com.gocardless.resources.Payment;
import com.google.errorprone.annotations.DoNotMock;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentStatesGraph;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExpireService {

    private TransactionService transactionService;
    private MandateStatesGraph mandateStatesGraph;
    private PaymentStatesGraph paymentStatesGraph;
    private static final Logger LOGGER = PayLoggerFactory.getLogger(ExpireService.class);
    private final long MIN_EXPIRY_AGE_MINUTES = 90l;
    private final PaymentState PAYMENT_EXPIRY_CUTOFF_STATUS = PaymentState.PENDING;
    
    @Inject
    public ExpireService(TransactionService transactionService, MandateStatesGraph mandateStatesGraph, PaymentStatesGraph paymentStatesGraph) {
        this.transactionService = transactionService;
        this.mandateStatesGraph = mandateStatesGraph;
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

    public List<Transaction> getPaymentsForExpiration() {
        Set<PaymentState> states = paymentStatesGraph.getPriorStates(PAYMENT_EXPIRY_CUTOFF_STATUS);
        ZonedDateTime cutOffTime = ZonedDateTime.now().minusMinutes(MIN_EXPIRY_AGE_MINUTES);
        return transactionService.findAllPaymentsBySetOfStatesAndCreationTime(states, cutOffTime);
    }
    
    public void expireMandates() {
        List<Mandate> mandatesToExpire = getMandatesForExpiration();
        for (Mandate mandate : mandatesToExpire) {
            expireMandate(mandate);
        }
    }

    private List<Mandate> getMandatesForExpiration() {
        List<Mandate> mandatesToExpire = new ArrayList<>();

        return mandatesToExpire;
    }

    private void expireMandate(Mandate mandate) {

    }
}
