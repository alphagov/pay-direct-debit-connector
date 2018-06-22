package uk.gov.pay.directdebit.payments.dao;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentStatesGraph;
import uk.gov.pay.directdebit.payments.model.Transaction;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class TransactionDaoIT {

    private static final PaymentState STATE = PaymentState.NEW;
    private static final long AMOUNT = 10L;

    @DropwizardTestContext
    private TestContext testContext;
    private TransactionDao transactionDao;

    private GatewayAccountFixture testGatewayAccount;
    private TransactionFixture testTransaction;
    private MandateFixture testMandate;

    @Before
    public void setup() {
        transactionDao = testContext.getJdbi().onDemand(TransactionDao.class);
        testGatewayAccount = aGatewayAccountFixture().insert(testContext.getJdbi());
        testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount).insert(testContext.getJdbi());
        testTransaction = generateNewTransactionFixture(testMandate, STATE, AMOUNT);
    }

    @Test
    public void shouldInsertATransaction() {
        Transaction transaction = testTransaction.toEntity();
        Long id = transactionDao.insert(transaction);
        Map<String, Object> foundTransaction = testContext.getDatabaseTestHelper().getTransactionById(id);
        assertThat(foundTransaction.get("id"), is(id));
        assertThat(foundTransaction.get("mandate_id"), is(testMandate.getId()));
        assertThat((Long) foundTransaction.get("amount"), isNumber(AMOUNT));
        assertThat(PaymentState.valueOf((String) foundTransaction.get("state")), is(STATE));
    }

    @Test
    public void shouldGetATransactionById() {
        testTransaction.insert(testContext.getJdbi());
        Transaction transaction = transactionDao.findById(testTransaction.getId()).get();
        assertThat(transaction.getId(), is(testTransaction.getId()));
        assertThat(transaction.getMandate(), is(testMandate.toEntity()));
        assertThat(transaction.getExternalId(), is(testTransaction.getExternalId()));
        assertThat(transaction.getDescription(), is(testTransaction.getDescription()));
        assertThat(transaction.getReference(), is(testTransaction.getReference()));
        assertThat(transaction.getAmount(), is(AMOUNT));
        assertThat(transaction.getState(), is(STATE));
        assertThat(transaction.getCreatedDate(), is(testTransaction.getCreatedDate()));
    }

    @Test
    public void shouldGetATransactionByExternalId() {
        testTransaction.insert(testContext.getJdbi());
        Transaction transaction = transactionDao.findByExternalId(testTransaction.getExternalId()).get();
        assertThat(transaction.getId(), is(testTransaction.getId()));
        assertThat(transaction.getMandate(), is(testMandate.toEntity()));
        assertThat(transaction.getExternalId(), is(testTransaction.getExternalId()));
        assertThat(transaction.getDescription(), is(testTransaction.getDescription()));
        assertThat(transaction.getReference(), is(testTransaction.getReference()));
        assertThat(transaction.getAmount(), is(AMOUNT));
        assertThat(transaction.getState(), is(STATE));
        assertThat(transaction.getCreatedDate(), is(testTransaction.getCreatedDate()));
    }

    @Test
    public void shouldFindAllTransactionsByPaymentStateAndProvider() {
        GatewayAccountFixture goCardlessGatewayAccount = aGatewayAccountFixture().withPaymentProvider(PaymentProvider.GOCARDLESS).insert(testContext.getJdbi());
        GatewayAccountFixture sandboxGatewayAccount = aGatewayAccountFixture().withPaymentProvider(PaymentProvider.SANDBOX).insert(testContext.getJdbi());

        MandateFixture sandboxMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(sandboxGatewayAccount).insert(testContext.getJdbi());
        MandateFixture goCardlessMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(goCardlessGatewayAccount).insert(testContext.getJdbi());
        TransactionFixture sandboxCharge =
                generateNewTransactionFixture(sandboxMandate, PaymentState.NEW, AMOUNT);
        
        generateNewTransactionFixture(goCardlessMandate, PaymentState.NEW, AMOUNT);
        sandboxCharge.insert(testContext.getJdbi());

        TransactionFixture successSandboxCharge =
                generateNewTransactionFixture(sandboxMandate, PaymentState.SUCCESS, AMOUNT);
        successSandboxCharge.insert(testContext.getJdbi());

        TransactionFixture goCardlessSuccessCharge =
                generateNewTransactionFixture(goCardlessMandate, PaymentState.SUCCESS, AMOUNT);
        goCardlessSuccessCharge.insert(testContext.getJdbi());

        List<Transaction> successTransactionsList = transactionDao.findAllByPaymentStateAndProvider(PaymentState.SUCCESS, PaymentProvider.SANDBOX);
        assertThat(successTransactionsList.size(), is(1));
        assertThat(successTransactionsList.get(0).getState(), is(PaymentState.SUCCESS));
        assertThat(successTransactionsList.get(0).getMandate().getGatewayAccount().getPaymentProvider(), is(PaymentProvider.SANDBOX));
    }

    @Test
    public void shouldNotFindAnyTransactionByPaymentState_ifPaymentStateIsNotUsed() {
        TransactionFixture processingDirectDebitPaymentStateTransactionFixture =
                generateNewTransactionFixture(testMandate, PaymentState.NEW, AMOUNT);
        processingDirectDebitPaymentStateTransactionFixture.insert(testContext.getJdbi());

        List<Transaction> successTransactionsList = transactionDao.findAllByPaymentStateAndProvider(PaymentState.SUCCESS, PaymentProvider.SANDBOX);
        assertThat(successTransactionsList.size(), is(0));
    }

    @Test
    public void shouldUpdateStateAndReturnNumberOfAffectedRows() {
        PaymentState newState = PaymentState.NEW;
        testTransaction.insert(testContext.getJdbi());
        int numOfUpdatedTransactions = transactionDao.updateState(testTransaction.getId(), newState);
        Map<String, Object> transactionAfterUpdate = testContext.getDatabaseTestHelper().getTransactionById(testTransaction.getId());
        assertThat(numOfUpdatedTransactions, is(1));
        assertThat(transactionAfterUpdate.get("id"), is(testTransaction.getId()));
        assertThat(transactionAfterUpdate.get("external_id"), is(testTransaction.getExternalId()));
        assertThat(transactionAfterUpdate.get("mandate_id"), is(testMandate.getId()));
        assertThat(transactionAfterUpdate.get("description"), is(testTransaction.getDescription()));
        assertThat(transactionAfterUpdate.get("reference"), is(testTransaction.getReference()));
        assertThat(transactionAfterUpdate.get("amount"), is(AMOUNT));
        assertThat(transactionAfterUpdate.get("state"), is(newState.toString()));
        assertThat((Timestamp) transactionAfterUpdate.get("created_date"), isDate(testTransaction.getCreatedDate()));
    }

    @Test
    public void shouldNotUpdateAnythingIfTransactionDoesNotExist() {
        int numOfUpdatedTransactions = transactionDao.updateState(34L, PaymentState.NEW);
        assertThat(numOfUpdatedTransactions, is(0));
    }
    
    @Test
    public void findAllPaymentsBySetOfStatesAndCreationTime_shouldFindThreePayments() {
        aTransactionFixture().withMandateFixture(testMandate).withState(PaymentState.NEW)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L)).insert(testContext.getJdbi());
        
        aTransactionFixture().withMandateFixture(testMandate).withState(PaymentState.NEW)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L)).insert(testContext.getJdbi());
        
        aTransactionFixture().withMandateFixture(testMandate).withState(PaymentState.NEW)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L)).insert(testContext.getJdbi());
        
        PaymentStatesGraph paymentStatesGraph = new PaymentStatesGraph();
        Set<PaymentState> states = paymentStatesGraph.getPriorStates(PaymentState.PENDING);
        List<Transaction> transactions = transactionDao.findAllPaymentsBySetOfStatesAndCreationTime(states, ZonedDateTime.now().minusMinutes(90L));
        assertThat(transactions.size(), is(3));
    }

    @Test
    public void findAllPaymentsBySetOfStatesAndCreationTime_shouldNotFindPayment_TooEarly() {
        aTransactionFixture().withMandateFixture(testMandate).withState(PaymentState.NEW)
                .withCreatedDate(ZonedDateTime.now()).insert(testContext.getJdbi());

        PaymentStatesGraph paymentStatesGraph = new PaymentStatesGraph();
        Set<PaymentState> states = paymentStatesGraph.getPriorStates(PaymentState.PENDING);
        List<Transaction> transactions = transactionDao.findAllPaymentsBySetOfStatesAndCreationTime(states, ZonedDateTime.now().minusMinutes(90L));
        assertThat(transactions.size(), is(0));
    }

    @Test
    public void findAllPaymentsBySetOfStatesAndCreationTime_shouldNotFindPayment_WrongState() {
        aTransactionFixture().withMandateFixture(testMandate).withState(PaymentState.PENDING)
                .withCreatedDate(ZonedDateTime.now()).insert(testContext.getJdbi());

        PaymentStatesGraph paymentStatesGraph = new PaymentStatesGraph();
        Set<PaymentState> states = paymentStatesGraph.getPriorStates(PaymentState.PENDING);
        List<Transaction> transactions = transactionDao.findAllPaymentsBySetOfStatesAndCreationTime(states, ZonedDateTime.now().minusMinutes(90L));
        assertThat(transactions.size(), is(0));
    }
    

    private TransactionFixture generateNewTransactionFixture(MandateFixture mandateFixture,
            PaymentState paymentState,
            long amount) {
        return aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withAmount(amount)
                .withState(paymentState);
    }

}
