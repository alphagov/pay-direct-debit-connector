package uk.gov.pay.directdebit.payments.dao;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
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
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.EventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.Event;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.EventFixture.aPaymentRequestEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_ACKNOWLEDGED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.Event.Type.MANDATE;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class EventDaoIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private EventDao eventDao;

    private MandateFixture testMandate;
    private TransactionFixture testTransaction;
    private GatewayAccountFixture gatewayAccountFixture;

    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setup() {
        eventDao = testContext.getJdbi().onDemand(EventDao.class);
        this.gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());
        this.testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
        this.testTransaction = TransactionFixture.aTransactionFixture()
                .withMandateFixture(testMandate)
                .insert(testContext.getJdbi());
    }

    @Test
    public void shouldInsertAnEvent() {
        EventFixture paymentRequestEvent = EventFixture
                .aPaymentRequestEventFixture()
                .withTransactionId(testTransaction.getId())
                .withMandateId(testMandate.getId());
        Long id = eventDao.insert(paymentRequestEvent.toEntity());
        Map<String, Object> foundPaymentRequestEvent = testContext.getDatabaseTestHelper().getEventById(id);
        assertThat(foundPaymentRequestEvent.get("id"), is(id));
        assertThat(foundPaymentRequestEvent.get("mandate_id"), is(testMandate.getId()));
        assertThat(foundPaymentRequestEvent.get("transaction_id"), is(testTransaction.getId()));
        assertThat(foundPaymentRequestEvent.get("event_type"), is(paymentRequestEvent.getEventType().toString()));
        assertThat(foundPaymentRequestEvent.get("event"), is(paymentRequestEvent.getEvent().toString()));
        assertThat((Timestamp) foundPaymentRequestEvent.get("event_date"), isDate(paymentRequestEvent.getEventDate()));
    }

    @Test
    public void shouldFindByMandateIdAndEvent() {
        aPaymentRequestEventFixture()
                .withMandateId(testMandate.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .insert(testContext.getJdbi());

        Optional<Event> event = eventDao
                .findByMandateIdAndEvent(testMandate.getId(), MANDATE,
                        PAYMENT_ACKNOWLEDGED_BY_PROVIDER);

        assertThat(event.isPresent(), is(true));
    }

    @Test
    public void shouldNotFindByMandateIdAndEvent_ifMandateDoesNotExist() {
        aPaymentRequestEventFixture()
                .withMandateId(testMandate.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .insert(testContext.getJdbi());

        Optional<Event> event = eventDao
                .findByMandateIdAndEvent(456L, MANDATE,
                        PAYMENT_ACKNOWLEDGED_BY_PROVIDER);

        assertThat(event.isPresent(), is(false));
    }

    @Test
    public void shouldNotFindByMandateIdAndEvent_ifEventIsWrong() {
        aPaymentRequestEventFixture()
                .withMandateId(testMandate.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .insert(testContext.getJdbi());

        Optional<Event> event = eventDao
                .findByMandateIdAndEvent(testMandate.getId(), MANDATE,
                        MANDATE_FAILED);

        assertThat(event.isPresent(), is(false));
    }
}
