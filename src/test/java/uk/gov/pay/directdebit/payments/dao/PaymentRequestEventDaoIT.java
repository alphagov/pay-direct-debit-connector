package uk.gov.pay.directdebit.payments.dao;

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
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestEventFixture.aPaymentRequestEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type.CHARGE;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.payerCreated;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentRequestEventDaoIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private PaymentRequestEventDao paymentRequestEventDao;

    private PaymentRequestFixture testPaymentRequest;

    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setup() {
        paymentRequestEventDao = testContext.getJdbi().onDemand(PaymentRequestEventDao.class);
        this.testPaymentRequest = aPaymentRequestFixture()
                .withGatewayAccountId(RandomUtils.nextLong(1, 99999))
                .insert(testContext.getJdbi());
    }

    @Test
    public void shouldInsertAnEvent() {
        Long paymentRequestId = testPaymentRequest.getId();
        PaymentRequestEvent paymentRequestEvent = payerCreated(paymentRequestId);
        Long id = paymentRequestEventDao.insert(paymentRequestEvent);
        Map<String, Object> foundPaymentRequestEvent = testContext.getDatabaseTestHelper().getPaymentRequestEventById(id);
        assertThat(foundPaymentRequestEvent.get("id"), is(id));
        assertThat(foundPaymentRequestEvent.get("payment_request_id"), is(paymentRequestId));
        assertThat(foundPaymentRequestEvent.get("event_type"), is(paymentRequestEvent.getEventType().toString()));
        assertThat(foundPaymentRequestEvent.get("event"), is(paymentRequestEvent.getEvent().toString()));
        assertThat((Timestamp) foundPaymentRequestEvent.get("event_date"), isDate(paymentRequestEvent.getEventDate()));
    }

    @Test
    public void shouldFindByPaymentRequestIdAndEvent() {

        aPaymentRequestEventFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .withEventType(CHARGE)
                .withEvent(PAYMENT_PENDING)
                .insert(testContext.getJdbi());

        Optional<PaymentRequestEvent> event = paymentRequestEventDao.findByPaymentRequestIdAndEvent(testPaymentRequest.getId(), CHARGE, PAYMENT_PENDING);

        assertThat(event.isPresent(), is(true));
    }
}
