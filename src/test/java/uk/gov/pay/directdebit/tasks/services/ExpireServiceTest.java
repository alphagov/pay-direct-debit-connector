package uk.gov.pay.directdebit.tasks.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentStatesGraph;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ExpireServiceTest {

    @DropwizardTestContext
    private TestContext testContext;
    private MandateStatesGraph mandateStatesGraph = new MandateStatesGraph();
    private PaymentStatesGraph paymentStatesGraph = new PaymentStatesGraph();
    
    @Mock
    TransactionService transactionService;
    
    private ExpireService expireService;
    
    @Before
    public void setup() {
        expireService = new ExpireService(transactionService, paymentStatesGraph);
    }
    
    
    @Test
    public void expirePayments_shouldCallTransactionServiceWithPriorStatesToPending() {
        Transaction transaction = TransactionFixture.aTransactionFixture().withState(PaymentState.NEW).toEntity();
        when(transactionService
                .findAllPaymentsBySetOfStatesAndCreationTime(eq(paymentStatesGraph.getPriorStates(PaymentState.PENDING)), any()))
                .thenReturn(Collections.singletonList(transaction));
        
        int numberOfExpiredPayments = expireService.expirePayments();
        assertEquals(1, numberOfExpiredPayments);
    }

    @Test
    public void shouldGetCorrectPaymentsStatesPriorToPending() {
        Set<PaymentState> paymentStates = paymentStatesGraph.getPriorStates(PaymentState.PENDING);
        Set<PaymentState> expectedPaymentStates = new HashSet<>(Arrays.asList(PaymentState.NEW));
        assertEquals(expectedPaymentStates, paymentStates);
    }

}
