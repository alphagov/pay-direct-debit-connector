package uk.gov.pay.directdebit.payments.dao;

import liquibase.exception.LiquibaseException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.infra.IntegrationTest;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.tokens.fixtures.TokenFixture;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;

public class TransactionDaoIT extends IntegrationTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private TransactionDao transactionDao;

    private PaymentRequestFixture testPaymentRequest;
    private TokenFixture testToken;
    private TransactionFixture testTransaction;

    @Before
    public void setup() throws IOException, LiquibaseException {
        transactionDao = jdbi.onDemand(TransactionDao.class);
        this.testPaymentRequest = aPaymentRequestFixture()
                .withGatewayAccountId(RandomUtils.nextLong(1, 99999))
                .insert(jdbi);
        this.testToken = TokenFixture.aTokenFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .insert(jdbi);
        this.testTransaction = TransactionFixture.aTransactionFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .insert(jdbi);
    }

    @Test
    public void shouldInsertATransaction() {
        Long paymentRequestId = testPaymentRequest.getId();
        Long amount = 13L;
        Transaction.Type type = Transaction.Type.CHARGE;
        PaymentState state = PaymentState.NEW;
        Transaction transaction = new Transaction(paymentRequestId, "externalId", amount, type, state);
        Long id = transactionDao.insert(transaction);
        Map<String, Object> foundTransaction = databaseTestHelper.getTransactionById(id);
        assertThat(foundTransaction.get("id"), is(id));
        assertThat(foundTransaction.get("payment_request_id"), is(paymentRequestId));
        assertThat((Long) foundTransaction.get("amount"), isNumber(amount));
        assertThat(Transaction.Type.valueOf((String) foundTransaction.get("type")), is(type));
        assertThat(PaymentState.valueOf((String) foundTransaction.get("state")), is(state));
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
        PaymentState newState = PaymentState.IN_PROGRESS;
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
        int numOfUpdatedTransactions = transactionDao.updateState(34L, PaymentState.IN_PROGRESS);
        assertThat(numOfUpdatedTransactions, is(0));
    }
}
