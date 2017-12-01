package uk.gov.pay.directdebit.payments.dao;

import liquibase.exception.LiquibaseException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.infra.DaoITestBase;
import uk.gov.pay.directdebit.infra.DropwizardAppWithPostgresRule;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.paymentRequestFixture;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.*;

public class PaymentRequestEventDaoTest extends DaoITestBase {

    @Rule
    public DropwizardAppWithPostgresRule postgres;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private PaymentRequestEventDao paymentRequestEventDao;

    private PaymentRequestFixture testPaymentRequest;

    @Before
    public void setup() throws IOException, LiquibaseException {
        paymentRequestEventDao = jdbi.onDemand(PaymentRequestEventDao.class);
        this.testPaymentRequest = paymentRequestFixture(jdbi)
                .withGatewayAccountId(RandomUtils.nextLong(1, 99999))
                .insert();
    }

    @Test
    public void shouldInsertAnEvent() {
        Long paymentRequestId = testPaymentRequest.getId();
        PaymentRequestEvent.Type eventType = PaymentRequestEvent.Type.PAYER;
        PaymentRequestEvent.SupportedEvent event = PaymentRequestEvent.SupportedEvent.CHARGE_CREATED;
        ZonedDateTime eventDate = ZonedDateTime.now();
        PaymentRequestEvent paymentRequestEvent = new PaymentRequestEvent(paymentRequestId, eventType, event, eventDate);
        Long id = paymentRequestEventDao.insert(paymentRequestEvent);
        Map<String, Object> foundPaymentRequestEvent = databaseTestHelper.getPaymentRequestEventById(id);
        assertThat(foundPaymentRequestEvent.get("id"), is(id));
        assertThat(foundPaymentRequestEvent.get("payment_request_id"), is(paymentRequestId));
        assertThat(foundPaymentRequestEvent.get("event_type"), is(eventType.toString()));
        assertThat(foundPaymentRequestEvent.get("event"), is(event.toString()));
        assertThat((Timestamp) foundPaymentRequestEvent.get("event_date"), isDate(eventDate));
    }

}
