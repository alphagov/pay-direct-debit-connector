package uk.gov.pay.directdebit.payments.dao;

import liquibase.exception.LiquibaseException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.infra.DaoITestBase;
import uk.gov.pay.directdebit.infra.DropwizardAppWithPostgresRule;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.paymentRequestFixture;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;


public class TransactionDaoTest extends DaoITestBase {

    @Rule
    public DropwizardAppWithPostgresRule postgres;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private TransactionDao transactionDao;

    private PaymentRequestFixture testPaymentRequest;

    @Before
    public void setup() throws IOException, LiquibaseException {
        transactionDao = jdbi.onDemand(TransactionDao.class);
        this.testPaymentRequest = paymentRequestFixture(jdbi)
                .withGatewayAccountId(RandomUtils.nextLong(1, 99999))
                .insert();
    }

    @Test
    public void shouldInsertATransaction() {
        Long paymentRequestId = testPaymentRequest.getId();
        Long amount = 13L;
        Transaction.Type type = Transaction.Type.CHARGE;
        PaymentState state = PaymentState.NEW;
        Transaction transaction = new Transaction(paymentRequestId, amount, type, state);
        Long id = transactionDao.insert(transaction);
        Map<String, Object> foundTransaction = databaseTestHelper.getTransactionById(id);
        assertThat(foundTransaction.get("id"), is(id));
        assertThat(foundTransaction.get("payment_request_id"), is(paymentRequestId));
        assertThat((Long) foundTransaction.get("amount"), isNumber(amount));
        assertThat(Transaction.Type.valueOf((String) foundTransaction.get("type")), is(type));
        assertThat(PaymentState.valueOf((String) foundTransaction.get("state")), is(state));
    }
}
