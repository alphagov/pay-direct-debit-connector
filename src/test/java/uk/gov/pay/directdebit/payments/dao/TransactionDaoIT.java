package uk.gov.pay.directdebit.payments.dao;

import liquibase.exception.LiquibaseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.tokens.fixtures.TokenFixture;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.tokens.fixtures.TokenFixture.aTokenFixture;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class TransactionDaoIT {

    private static final Transaction.Type TYPE = Transaction.Type.CHARGE;
    private static final PaymentState STATE = PaymentState.AWAITING_DIRECT_DEBIT_DETAILS;
    private static final long AMOUNT = 10L;

    @DropwizardTestContext
    private TestContext testContext;
    private TransactionDao transactionDao;

    private GatewayAccountFixture testGatewayAccount;
    private PaymentRequestFixture testPaymentRequest;
    private TransactionFixture testTransaction;

    @Before
    public void setup() throws IOException, LiquibaseException {
        transactionDao = testContext.getJdbi().onDemand(TransactionDao.class);
        this.testGatewayAccount = aGatewayAccountFixture().insert(testContext.getJdbi());
        this.testPaymentRequest = generateNewPaymentRequestFixture(testGatewayAccount.getId());
        this.testTransaction = generateNewTransactionFixture(testPaymentRequest, TYPE, STATE, AMOUNT);
    }

    @Test
    public void shouldInsertATransaction() {
        Transaction transaction = testTransaction.toEntity();
        Long id = transactionDao.insert(transaction);
        Map<String, Object> foundTransaction = testContext.getDatabaseTestHelper().getTransactionById(id);
        assertThat(foundTransaction.get("id"), is(id));
        assertThat(foundTransaction.get("payment_request_id"), is(testPaymentRequest.getId()));
        assertThat((Long) foundTransaction.get("amount"), isNumber(AMOUNT));
        assertThat(Transaction.Type.valueOf((String) foundTransaction.get("type")), is(TYPE));
        assertThat(PaymentState.valueOf((String) foundTransaction.get("state")), is(STATE));
    }

    @Test
    public void shouldGetATransactionById() {
        testTransaction.insert(testContext.getJdbi());
        Transaction transaction = transactionDao.findById(testTransaction.getId()).get();
        PaymentRequest paymentRequest = transaction.getPaymentRequest();
        assertThat(transaction.getId(), is(testTransaction.getId()));
        assertThat(paymentRequest.getId(), is(testPaymentRequest.getId()));
        assertThat(paymentRequest.getExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transaction.getGatewayAccountId(), is(testPaymentRequest.getGatewayAccountId()));
        assertThat(paymentRequest.getDescription(), is(testPaymentRequest.getDescription()));
        assertThat(transaction.getType(), is(TYPE));
        assertThat(transaction.getAmount(), is(AMOUNT));
        assertThat(transaction.getState(), is(STATE));
    }

    @Test
    public void shouldGetATransactionByPaymentRequestId() {
        testTransaction.insert(testContext.getJdbi());
        Transaction transaction = transactionDao.findByPaymentRequestId(testPaymentRequest.getId()).get();
        PaymentRequest paymentRequest = transaction.getPaymentRequest();
        assertThat(transaction.getId(), is(testTransaction.getId()));
        assertThat(paymentRequest.getId(), is(testPaymentRequest.getId()));
        assertThat(paymentRequest.getExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transaction.getGatewayAccountId(), is(testPaymentRequest.getGatewayAccountId()));
        assertThat(paymentRequest.getDescription(), is(testPaymentRequest.getDescription()));
        assertThat(transaction.getType(), is(TYPE));
        assertThat(transaction.getAmount(), is(AMOUNT));
        assertThat(transaction.getState(), is(STATE));
    }

    @Test
    public void shouldGetATransactionByPaymentRequestExternalIdAndGatewayAccountId() {
        testTransaction.insert(testContext.getJdbi());
        Transaction transaction = transactionDao.findTransactionForExternalIdAndGatewayAccountExternalId(testPaymentRequest.getExternalId(), testGatewayAccount.getExternalId()).get();
        PaymentRequest paymentRequest = transaction.getPaymentRequest();
        assertThat(transaction.getId(), is(testTransaction.getId()));
        assertThat(paymentRequest.getId(), is(testTransaction.getPaymentRequestId()));
        assertThat(paymentRequest.getExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transaction.getGatewayAccountId(), is(testPaymentRequest.getGatewayAccountId()));
        assertThat(paymentRequest.getDescription(), is(testPaymentRequest.getDescription()));
        assertThat(transaction.getType(), is(TYPE));
        assertThat(transaction.getAmount(), is(AMOUNT));
        assertThat(transaction.getState(), is(STATE));
    }

    @Test
    public void shouldFindATransactionByTokenId() {
        TokenFixture token = aTokenFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .insert(testContext.getJdbi());
        testTransaction.insert(testContext.getJdbi());
        Transaction transaction = transactionDao.findByTokenId(token.getToken()).get();
        PaymentRequest paymentRequest = transaction.getPaymentRequest();
        assertThat(transaction.getId(), is(testTransaction.getId()));
        assertThat(paymentRequest.getId(), is(testPaymentRequest.getId()));
        assertThat(paymentRequest.getExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transaction.getGatewayAccountId(), is(testPaymentRequest.getGatewayAccountId()));
        assertThat(paymentRequest.getDescription(), is(testPaymentRequest.getDescription()));
        assertThat(transaction.getType(), is(TYPE));
        assertThat(transaction.getAmount(), is(AMOUNT));
        assertThat(transaction.getState(), is(STATE));
    }

    @Test
    public void shouldNotFindATransactionByTokenId_ifTokenIdIsInvalid() {
        String tokenId = "non_existing_tokenId";
        assertThat(transactionDao.findByTokenId(tokenId), is(Optional.empty()));
    }

    @Test
    public void shouldFindAllTransactionsByPaymentStateAndProvider() {
        GatewayAccountFixture goCardlessGatewayAccount = aGatewayAccountFixture().withPaymentProvider(PaymentProvider.GOCARDLESS).insert(testContext.getJdbi());

        PaymentRequestFixture sandboxPaymentRequest = generateNewPaymentRequestFixture(testGatewayAccount.getId());
        TransactionFixture sandboxCharge =
                generateNewTransactionFixture(sandboxPaymentRequest, TYPE, PaymentState.SUBMITTING_DIRECT_DEBIT_PAYMENT, AMOUNT);
        sandboxCharge.insert(testContext.getJdbi());

        PaymentRequestFixture successSandboxPaymentRequest = generateNewPaymentRequestFixture(testGatewayAccount.getId());
        TransactionFixture successSandboxCharge =
                generateNewTransactionFixture(successSandboxPaymentRequest, TYPE, PaymentState.SUCCESS, AMOUNT);
        successSandboxCharge.insert(testContext.getJdbi());

        PaymentRequestFixture gocardlessPaymentRequest = generateNewPaymentRequestFixture(goCardlessGatewayAccount.getId());
        TransactionFixture goCardlessSuccessCharge =
                generateNewTransactionFixture(gocardlessPaymentRequest, TYPE, PaymentState.SUCCESS, AMOUNT);
        goCardlessSuccessCharge.insert(testContext.getJdbi());

        List<Transaction> successTransactionsList = transactionDao.findAllByPaymentStateAndProvider(PaymentState.SUCCESS, PaymentProvider.SANDBOX);
        assertThat(successTransactionsList.size(), is(1));
        assertThat(successTransactionsList.get(0).getState(), is(PaymentState.SUCCESS));
        assertThat(successTransactionsList.get(0).getPaymentProvider(), is(PaymentProvider.SANDBOX));
    }

    @Test
    public void shouldNotFindAnyTransactionByPaymentState_ifPaymentStateIsNotUsed() {
        PaymentRequestFixture processingDirectDebitPaymentStatePaymentRequestFixture = generateNewPaymentRequestFixture(testGatewayAccount.getId());
        TransactionFixture processingDirectDebitPaymentStateTransactionFixture =
                generateNewTransactionFixture(processingDirectDebitPaymentStatePaymentRequestFixture, TYPE, PaymentState.SUBMITTING_DIRECT_DEBIT_PAYMENT, AMOUNT);
        processingDirectDebitPaymentStateTransactionFixture.insert(testContext.getJdbi());

        List<Transaction> successTransactionsList = transactionDao.findAllByPaymentStateAndProvider(PaymentState.SUCCESS, PaymentProvider.SANDBOX);
        assertThat(successTransactionsList.size(), is(0));
    }

    @Test
    public void shouldUpdateStateAndReturnNumberOfAffectedRows() {
        PaymentState newState = PaymentState.AWAITING_DIRECT_DEBIT_DETAILS;
        testTransaction.insert(testContext.getJdbi());
        int numOfUpdatedTransactions = transactionDao.updateState(testTransaction.getId(), newState);
        Transaction transactionAfterUpdate = transactionDao.findByPaymentRequestId(testTransaction.getPaymentRequestId()).get();
        PaymentRequest paymentRequest = transactionAfterUpdate.getPaymentRequest();
        assertThat(numOfUpdatedTransactions, is(1));
        assertThat(transactionAfterUpdate.getId(), is(testTransaction.getId()));
        assertThat(paymentRequest.getId(), is(testPaymentRequest.getId()));
        assertThat(paymentRequest.getExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transactionAfterUpdate.getGatewayAccountId(), is(testPaymentRequest.getGatewayAccountId()));
        assertThat(paymentRequest.getDescription(), is(testPaymentRequest.getDescription()));
        assertThat(transactionAfterUpdate.getType(), is(TYPE));
        assertThat(transactionAfterUpdate.getAmount(), is(AMOUNT));
        assertThat(transactionAfterUpdate.getState(), is(newState));
    }

    @Test
    public void shouldNotUpdateAnythingIfTransactionDoesNotExist() {
        int numOfUpdatedTransactions = transactionDao.updateState(34L, PaymentState.AWAITING_DIRECT_DEBIT_DETAILS);
        assertThat(numOfUpdatedTransactions, is(0));
    }

    private PaymentRequestFixture generateNewPaymentRequestFixture(Long accountId) {
        return aPaymentRequestFixture()
                .withGatewayAccountId(accountId)
                .insert(testContext.getJdbi());
    }

    private TransactionFixture generateNewTransactionFixture(PaymentRequestFixture paymentRequestFixture,
                                                             Transaction.Type type,
                                                             PaymentState paymentState,
                                                             long amount) {
        return aTransactionFixture()
                .withPaymentRequestId(paymentRequestFixture.getId())
                .withPaymentRequestExternalId(paymentRequestFixture.getExternalId())
                .withGatewayAccountId(paymentRequestFixture.getGatewayAccountId())
                .withPaymentRequestDescription(paymentRequestFixture.getDescription())
                .withAmount(amount)
                .withState(paymentState)
                .withType(type);
    }

}
