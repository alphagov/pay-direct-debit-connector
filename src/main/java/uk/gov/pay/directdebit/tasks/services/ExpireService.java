package uk.gov.pay.directdebit.tasks.services;

import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.payments.model.PaymentState;
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
    
    @Inject
    public ExpireService(TransactionService transactionService, MandateStatesGraph mandateStatesGraph) {
        this.transactionService = transactionService;
        this.mandateStatesGraph = mandateStatesGraph;
    }
    

    
    public void expirePayments() {
        List<Transaction> paymentsToExpire = getPaymentsForExpiration();
        for (Transaction payment : paymentsToExpire) {
            expirePayment(payment);
        }
    }

    private List<Transaction> getPaymentsForExpiration() {
        Set<MandateState> states = mandateStatesGraph.getPriorStates(MandateState.PENDING);
        ZonedDateTime cutOffTime = ZonedDateTime.now().minusMinutes(90l);
        return transactionService.findAllPaymentsBySetOfStatesAndCreationTime(new ArrayList<>(states), cutOffTime);
    }

    private void expirePayment(Transaction payment) {
        payment.setState(PaymentState.CANCELLED);
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
