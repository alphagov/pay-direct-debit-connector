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
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
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
    public void setup() throws IOException, LiquibaseException {
        paymentRequestEventDao = testContext.getJdbi().onDemand(PaymentRequestEventDao.class);
        this.testPaymentRequest = aPaymentRequestFixture()
                .withGatewayAccountId(RandomUtils.nextLong(1, 99999))
                .insert(testContext.getJdbi());
    }

    @Test
    public void shouldInsertAnEvent() {
        Long paymentRequestId = testPaymentRequest.getId();
        PaymentRequestEvent.Type eventType = PaymentRequestEvent.Type.CHARGE;
        PaymentRequestEvent.SupportedEvent event = PaymentRequestEvent.SupportedEvent.CHARGE_CREATED;
        ZonedDateTime eventDate = ZonedDateTime.now();
        PaymentRequestEvent paymentRequestEvent = new PaymentRequestEvent(paymentRequestId, eventType, event, eventDate);
        Long id = paymentRequestEventDao.insert(paymentRequestEvent);
        Map<String, Object> foundPaymentRequestEvent = testContext.getDatabaseTestHelper().getPaymentRequestEventById(id);
        assertThat(foundPaymentRequestEvent.get("id"), is(id));
        assertThat(foundPaymentRequestEvent.get("payment_request_id"), is(paymentRequestId));
        assertThat(foundPaymentRequestEvent.get("event_type"), is(eventType.toString()));
        assertThat(foundPaymentRequestEvent.get("event"), is(event.toString()));
        assertThat((Timestamp) foundPaymentRequestEvent.get("event_date"), isDate(eventDate));
    }

}
