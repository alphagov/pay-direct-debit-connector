package uk.gov.pay.directdebit.payments.dao;

import liquibase.exception.LiquibaseException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.tokens.fixtures.TokenFixture.*;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class TransactionDaoIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @DropwizardTestContext
    private TestContext testContext;
    private TransactionDao transactionDao;

    private PaymentRequestFixture testPaymentRequest;
    private TokenFixture testToken;
    private TransactionFixture testTransaction;

    @Before
    public void setup() throws IOException, LiquibaseException {
        transactionDao = testContext.getJdbi().onDemand(TransactionDao.class);
        this.testPaymentRequest = aPaymentRequestFixture()
                .withGatewayAccountId(RandomUtils.nextLong(1, 99999))
                .insert(testContext.getJdbi());
        this.testToken = aTokenFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .insert(testContext.getJdbi());
        this.testTransaction = aTransactionFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .insert(testContext.getJdbi());
    }

    @Test
    public void shouldInsertATransaction() {
        Transaction transaction = aTransactionFixture().withPaymentRequestId(testPaymentRequest.getId()).toEntity();
        Long id = transactionDao.insert(transaction);
        Map<String, Object> foundTransaction = testContext.getDatabaseTestHelper().getTransactionById(id);
        assertThat(foundTransaction.get("id"), is(id));
        assertThat(foundTransaction.get("payment_request_id"), is(testPaymentRequest.getId()));
        assertThat((Long) foundTransaction.get("amount"), isNumber(transaction.getAmount()));
        assertThat(Transaction.Type.valueOf((String) foundTransaction.get("type")), is(transaction.getType()));
        assertThat(PaymentState.valueOf((String) foundTransaction.get("state")), is(transaction.getState()));
    }

    @Test
    public void shouldGetATransactionByPaymentRequestId() {
        Transaction transaction = transactionDao.findByPaymentRequestId(testPaymentRequest.getId()).get();
        assertThat(transaction.getId(), is(testTransaction.getId()));
        assertThat(transaction.getPaymentRequestId(), is(testTransaction.getPaymentRequestId()));
        assertThat(transaction.getPaymentRequestExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transaction.getType(), is(testTransaction.getType()));
        assertThat(transaction.getAmount(), is(testTransaction.getAmount()));
        assertThat(transaction.getState(), is(testTransaction.getState()));
    }

    @Test
    public void shouldGetATransactionByPaymentRequestExternalId() {
        Transaction transaction = transactionDao.findByPaymentRequestExternalId(testPaymentRequest.getExternalId()).get();
        assertThat(transaction.getId(), is(testTransaction.getId()));
        assertThat(transaction.getPaymentRequestId(), is(testTransaction.getPaymentRequestId()));
        assertThat(transaction.getPaymentRequestExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transaction.getType(), is(testTransaction.getType()));
        assertThat(transaction.getAmount(), is(testTransaction.getAmount()));
        assertThat(transaction.getState(), is(testTransaction.getState()));
    }
    @Test
    public void shouldFindATransactionByTokenId() {
        Transaction transaction = transactionDao.findByTokenId(testToken.getToken()).get();
        assertThat(transaction.getId(), is(notNullValue()));
        assertThat(transaction.getPaymentRequestId(), is(testPaymentRequest.getId()));
        assertThat(transaction.getPaymentRequestExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transaction.getAmount(), is(testTransaction.getAmount()));
        assertThat(transaction.getState(), is(testTransaction.getState()));
        assertThat(transaction.getType(), is(testTransaction.getType()));
    }

    @Test
    public void shouldNotFindATransactionByTokenId_ifTokenIdIsInvalid() {
        String tokenId = "non_existing_tokenId";
        assertThat(transactionDao.findByTokenId(tokenId), is(Optional.empty()));
    }

    @Test
    public void shouldUpdateStateAndReturnNumberOfAffectedRows() {
        PaymentState newState = PaymentState.AWAITING_DIRECT_DEBIT_DETAILS;
        int numOfUpdatedTransactions = transactionDao.updateState(testTransaction.getId(), newState);
        Transaction transactionAfterUpdate = transactionDao.findByPaymentRequestId(testTransaction.getPaymentRequestId()).get();
        assertThat(numOfUpdatedTransactions, is(1));
        assertThat(transactionAfterUpdate.getId(), is(testTransaction.getId()));
        assertThat(transactionAfterUpdate.getPaymentRequestExternalId(), is(testPaymentRequest.getExternalId()));
        assertThat(transactionAfterUpdate.getType(), is(testTransaction.getType()));
        assertThat(transactionAfterUpdate.getAmount(), is(testTransaction.getAmount()));
        assertThat(transactionAfterUpdate.getState(), is(newState));
    }

    @Test
    public void shouldNotUpdateAnythingIfTransactionDoesNotExist() {
        int numOfUpdatedTransactions = transactionDao.updateState(34L, PaymentState.AWAITING_DIRECT_DEBIT_DETAILS);
        assertThat(numOfUpdatedTransactions, is(0));
    }
}
