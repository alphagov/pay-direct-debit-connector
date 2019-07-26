package uk.gov.pay.directdebit.events.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.Payment;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_EXPIRED_BY_SYSTEM;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GovUkPayEventFixture.aGovUkPayEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GovUkPayEventDaoITest {

    @DropwizardTestContext
    private TestContext testContext;

    private GovUkPayEventDao govUkPayEventDao;
    private Mandate mandate;
    private Payment payment;

    @Before
    public void setUp() {
        govUkPayEventDao = testContext.getJdbi().onDemand(GovUkPayEventDao.class);

        var gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
        gatewayAccountFixture.insert(testContext.getJdbi());

        var mandateFixture = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());
        mandate = mandateFixture.toEntity();

        payment = aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .insert(testContext.getJdbi())
                .toEntity();
    }

    @Test
    public void shouldInsertAnEvent() {

        var govUkPayEvent = aGovUkPayEventFixture()
                .withMandateId(mandate.getId())
                .toEntity();

        Long id = govUkPayEventDao.insert(govUkPayEvent);

        Map<String, Object> govUkPayEventMap = testContext.getDatabaseTestHelper().getGovUkPayEventById(id);

        assertThat(govUkPayEventMap.get("id"), is(id));
        assertThat(govUkPayEventMap.get("mandate_id"), is(mandate.getId()));
        assertThat(govUkPayEventMap.get("payment_id"), is(nullValue()));
        assertThat(govUkPayEventMap.get("event_date"), is(Timestamp.from(govUkPayEvent.getEventDate().toInstant())));
        assertThat(govUkPayEventMap.get("resource_type"), is(govUkPayEvent.getResourceType().toString()));
        assertThat(govUkPayEventMap.get("event_type"), is(govUkPayEvent.getEventType().toString()));
    }

    @Test
    public void shouldFindLatestMandateEvent() {

        aGovUkPayEventFixture()
                .withMandateId(mandate.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T12:30:40Z"))
                .withEventType(MANDATE_SUBMITTED_TO_PROVIDER)
                .insert(testContext.getJdbi())
                .toEntity();
        var latestEvent = aGovUkPayEventFixture()
                .withMandateId(mandate.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T13:30:40Z"))
                .withEventType(MANDATE_CREATED)
                .insert(testContext.getJdbi())
                .toEntity();
        aGovUkPayEventFixture()
                .withMandateId(mandate.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T11:30:40Z"))
                .withEventType(MANDATE_CANCELLED_BY_USER)
                .insert(testContext.getJdbi())
                .toEntity();

        Optional<GovUkPayEvent> govUkPayEvent = govUkPayEventDao.findLatestEventForMandate(mandate.getId());

        assertThat(govUkPayEvent.get(), is(latestEvent));
    }

    @Test
    public void shouldReturnEmptyOptional_whenNoPreviousMandateEvent() {
        Optional<GovUkPayEvent> govUkPayEvent = govUkPayEventDao.findLatestEventForMandate(mandate.getId());
        assertThat(govUkPayEvent, is(Optional.empty()));
    }

    @Test
    public void shouldFindLatestPaymentEvent() {

        aGovUkPayEventFixture()
                .withPaymentId(payment.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T12:30:40Z"))
                .withEventType(PAYMENT_SUBMITTED)
                .insert(testContext.getJdbi())
                .toEntity();
        var earliestEvent = aGovUkPayEventFixture()
                .withPaymentId(payment.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T13:30:40Z"))
                .withEventType(PAYMENT_SUBMITTED)
                .insert(testContext.getJdbi())
                .toEntity();
        aGovUkPayEventFixture()
                .withPaymentId(payment.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T11:30:40Z"))
                .withEventType(PAYMENT_SUBMITTED)
                .insert(testContext.getJdbi())
                .toEntity();

        Optional<GovUkPayEvent> govUkPayEvent = govUkPayEventDao.findLatestEventForPayment(payment.getId());

        assertThat(govUkPayEvent.get(), is(earliestEvent));
    }

    @Test
    public void shouldReturnEmptyOptional_whenNoPreviousPaymentEvent() {
        Optional<GovUkPayEvent> govUkPayEvent = govUkPayEventDao.findLatestEventForPayment(payment.getId());
        assertThat(govUkPayEvent, is(Optional.empty()));
    }

    @Test
    public void shouldFindLatestApplicableEventForMandate() {
        aGovUkPayEventFixture()
                .withMandateId(mandate.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T14:30:40Z"))
                .withEventType(MANDATE_EXPIRED_BY_SYSTEM)
                .insert(testContext.getJdbi())
                .toEntity();
        var latestApplicableEvent = aGovUkPayEventFixture()
                .withMandateId(mandate.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T13:30:40Z"))
                .withEventType(MANDATE_CREATED)
                .insert(testContext.getJdbi())
                .toEntity();
        aGovUkPayEventFixture()
                .withMandateId(mandate.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T11:30:40Z"))
                .withEventType(MANDATE_CANCELLED_BY_USER)
                .insert(testContext.getJdbi())
                .toEntity();

        var applicableEvents = Set.of(MANDATE_CREATED, MANDATE_CANCELLED_BY_USER);
        Optional<GovUkPayEvent> govUkPayEvent = govUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), applicableEvents);

        assertThat(govUkPayEvent.get(), is(latestApplicableEvent));
    }

    @Test
    public void shouldReturnEmptyOptionalWhenNoApplicableEventForMandate() {
        aGovUkPayEventFixture()
                .withMandateId(mandate.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T14:30:40Z"))
                .withEventType(MANDATE_EXPIRED_BY_SYSTEM)
                .insert(testContext.getJdbi())
                .toEntity();

        var applicableEvents = Set.of(MANDATE_CREATED, MANDATE_CANCELLED_BY_USER);
        Optional<GovUkPayEvent> govUkPayEvent = govUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), applicableEvents);

        assertThat(govUkPayEvent, is(Optional.empty()));
    }

    @Test
    public void shouldFindLatestApplicableEventForPayment() {
        aGovUkPayEventFixture()
                .withPaymentId(payment.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T14:30:40Z"))
                .withEventType(MANDATE_SUBMITTED_TO_PROVIDER)
                .insert(testContext.getJdbi())
                .toEntity();
        var latestApplicableEvent = aGovUkPayEventFixture()
                .withPaymentId(payment.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T13:30:40Z"))
                .withEventType(PAYMENT_SUBMITTED)
                .insert(testContext.getJdbi())
                .toEntity();
        aGovUkPayEventFixture()
                .withPaymentId(payment.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T11:30:40Z"))
                .withEventType(PAYMENT_SUBMITTED)
                .insert(testContext.getJdbi())
                .toEntity();

        var applicableEvents = Set.of(PAYMENT_SUBMITTED);
        Optional<GovUkPayEvent> govUkPayEvent = govUkPayEventDao.findLatestApplicableEventForPayment(payment.getId(), applicableEvents);

        assertThat(govUkPayEvent.get(), is(latestApplicableEvent));
    }

    @Test
    public void shouldReturnEmptyOptionalWhenNoApplicableEventForPayment() {
        aGovUkPayEventFixture()
                .withPaymentId(payment.getId())
                .withEventDate(ZonedDateTime.parse("2019-01-01T14:30:40Z"))
                .withEventType(MANDATE_SUBMITTED_TO_PROVIDER)
                .insert(testContext.getJdbi())
                .toEntity();

        var applicableEvents = Set.of(PAYMENT_SUBMITTED);
        Optional<GovUkPayEvent> govUkPayEvent = govUkPayEventDao.findLatestApplicableEventForPayment(payment.getId(), applicableEvents);

        assertThat(govUkPayEvent, is(Optional.empty()));
    }
}
