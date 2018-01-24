package uk.gov.pay.directdebit.payments.dao;

import liquibase.exception.LiquibaseException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.tokens.fixtures.TokenFixture;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
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

    private PaymentRequestFixture testPaymentRequest;
    private TransactionFixture testTransaction;

    @Before
    public void setup() throws IOException, LiquibaseException {
        transactionDao = testContext.getJdbi().onDemand(TransactionDao.class);
        this.testPaymentRequest = aPaymentRequestFixture()
                .withGatewayAccountId(RandomUtils.nextLong(1, 99999))
                .insert(testContext.getJdbi());
        this.testTransaction = aTransactionFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .withPaymentRequestExternalId(testPaymentRequest.getExternalId())
                .withPaymentRequestGatewayAccountId(testPaymentRequest.getGatewayAccountId())
                .withPaymentRequestDescription(testPaymentRequest.getDescription())
                .withAmount(AMOUNT)
                .withState(STATE)
                .withType(TYPE);
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
        assertThat(foundTransaction.get("payment_request_id"), is(testTransaction.getPaymentRequestId()));
        assertThat((Long) foundTransaction.get("amount"), isNumber(testTransaction.getAmount()));
        assertThat(Transaction.Type.valueOf((String) foundTransaction.get("type")), is(testTransaction.getType()));
        assertThat(PaymentState.valueOf((String) foundTransaction.get("state")), is(testTransaction.getState()));
    }

    @Test
    public void shouldGetATransactionByPaymentRequestId() {
        testTransaction.insert(testContext.getJdbi());
        Transaction transaction = transactionDao.findByPaymentRequestId(testPaymentRequest.getId()).get();
        assertThat(transaction.getId(), is(testTransaction.getId()));
        assertThat(transaction.getPaymentRequestId(), is(testPaymentRequest.getId()));
        assertThat(transaction.getPaymentRequestExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transaction.getPaymentRequestGatewayAccountId(), is(testPaymentRequest.getGatewayAccountId()));
        assertThat(transaction.getPaymentRequestDescription(), is(testPaymentRequest.getDescription()));
        assertThat(transaction.getType(), is(TYPE));
        assertThat(transaction.getAmount(), is(AMOUNT));
        assertThat(transaction.getState(), is(STATE));
    }

    @Test
    public void shouldGetATransactionByPaymentRequestExternalId() {
        testTransaction.insert(testContext.getJdbi());
        Transaction transaction = transactionDao.findByPaymentRequestExternalId(testPaymentRequest.getExternalId()).get();
        assertThat(transaction.getId(), is(testTransaction.getId()));
        assertThat(transaction.getPaymentRequestId(), is(testTransaction.getPaymentRequestId()));
        assertThat(transaction.getPaymentRequestExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transaction.getPaymentRequestGatewayAccountId(), is(testPaymentRequest.getGatewayAccountId()));
        assertThat(transaction.getPaymentRequestDescription(), is(testPaymentRequest.getDescription()));
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
        assertThat(transaction.getId(), is(testTransaction.getId()));
        assertThat(transaction.getPaymentRequestId(), is(testPaymentRequest.getId()));
        assertThat(transaction.getPaymentRequestExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transaction.getPaymentRequestGatewayAccountId(), is(testPaymentRequest.getGatewayAccountId()));
        assertThat(transaction.getPaymentRequestDescription(), is(testPaymentRequest.getDescription()));
        assertThat(transaction.getType(), is(TYPE));
        assertThat(transaction.getAmount(), is(AMOUNT));
        assertThat(transaction.getState(), is(STATE));
    }

    @Test
    public void shouldNotFindATransactionByTokenId_ifTokenIdIsInvalid() {
        String tokenId = "non_existing_tokenId";
        assertThat(transactionDao.findByTokenId(tokenId).isPresent(), is(false));
    }

    @Test
    public void shouldUpdateStateAndReturnNumberOfAffectedRows() {
        PaymentState newState = PaymentState.AWAITING_DIRECT_DEBIT_DETAILS;
        testTransaction.insert(testContext.getJdbi());
        int numOfUpdatedTransactions = transactionDao.updateState(testTransaction.getId(), newState);
        Transaction transactionAfterUpdate = transactionDao.findByPaymentRequestId(testTransaction.getPaymentRequestId()).get();
        assertThat(numOfUpdatedTransactions, is(1));
        assertThat(transactionAfterUpdate.getId(), is(testTransaction.getId()));
        assertThat(transactionAfterUpdate.getPaymentRequestId(), is(testPaymentRequest.getId()));
        assertThat(transactionAfterUpdate.getPaymentRequestExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transactionAfterUpdate.getPaymentRequestGatewayAccountId(), is(testPaymentRequest.getGatewayAccountId()));
        assertThat(transactionAfterUpdate.getPaymentRequestDescription(), is(testPaymentRequest.getDescription()));
        assertThat(transactionAfterUpdate.getType(), is(TYPE));
        assertThat(transactionAfterUpdate.getAmount(), is(AMOUNT));
        assertThat(transactionAfterUpdate.getState(), is(newState));
    }

    @Test
    public void shouldNotUpdateAnythingIfTransactionDoesNotExist() {
        int numOfUpdatedTransactions = transactionDao.updateState(34L, PaymentState.AWAITING_DIRECT_DEBIT_DETAILS);
        assertThat(numOfUpdatedTransactions, is(0));
    }
}
