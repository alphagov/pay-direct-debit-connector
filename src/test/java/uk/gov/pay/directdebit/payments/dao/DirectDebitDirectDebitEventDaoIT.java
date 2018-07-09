package uk.gov.pay.directdebit.payments.dao;

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
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture.aDirectDebitEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_ACKNOWLEDGED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.MANDATE;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class DirectDebitDirectDebitEventDaoIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private DirectDebitEventDao directDebitEventDao;

    private MandateFixture testMandate;
    private TransactionFixture testTransaction;

    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setup() {
        directDebitEventDao = new DirectDebitEventDao(testContext.getJdbi());
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .insert(testContext.getJdbi());
        this.testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(
                gatewayAccountFixture).insert(testContext.getJdbi());
        this.testTransaction = TransactionFixture.aTransactionFixture()
                .withMandateFixture(testMandate)
                .insert(testContext.getJdbi());
    }

    @Test
    public void shouldInsertAnEvent() {
        DirectDebitEventFixture directDebitEvent = DirectDebitEventFixture
                .aDirectDebitEventFixture()
                .withTransactionId(testTransaction.getId())
                .withMandateId(testMandate.getId());
        Long id = directDebitEventDao.insert(directDebitEvent.toEntity());
        Map<String, Object> foundDirectDebitEvent = testContext.getDatabaseTestHelper().getEventById(id);
        assertThat(foundDirectDebitEvent.get("id"), is(id));
        assertThat(foundDirectDebitEvent.get("mandate_id"), is(testMandate.getId()));
        assertThat(foundDirectDebitEvent.get("transaction_id"), is(testTransaction.getId()));
        assertThat(foundDirectDebitEvent.get("event_type"), is(directDebitEvent.getEventType().toString()));
        assertThat(foundDirectDebitEvent.get("event"), is(directDebitEvent.getEvent().toString()));
        assertThat((Timestamp) foundDirectDebitEvent.get("event_date"), isDate(directDebitEvent.getEventDate()));
    }

    @Test
    public void shouldFindByMandateIdAndEvent() {
        DirectDebitEventFixture insert = aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .insert(testContext.getJdbi());

        DirectDebitEvent foundDirectDebitEvent = directDebitEventDao
                .findByMandateIdAndEvent(testMandate.getId(), MANDATE,
                        PAYMENT_ACKNOWLEDGED_BY_PROVIDER).get();

        assertThat(foundDirectDebitEvent.getMandateId(), is(testMandate.getId()));
        assertThat(foundDirectDebitEvent.getEvent(), is(PAYMENT_ACKNOWLEDGED_BY_PROVIDER));
        assertThat(foundDirectDebitEvent.getEventType(), is(MANDATE));
        assertThat(foundDirectDebitEvent.getExternalId(), is(insert.getExternalId()));
        assertThat(foundDirectDebitEvent.getMandateExternalId(), is(testMandate.getExternalId()));
        assertThat(foundDirectDebitEvent.getTransactionExternalId(), is(testTransaction.getExternalId()));
    }

    @Test
    public void shouldNotFindByMandateIdAndEvent_ifMandateDoesNotExist() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .insert(testContext.getJdbi());

        Optional<DirectDebitEvent> event = directDebitEventDao
                .findByMandateIdAndEvent(456L, MANDATE,
                        PAYMENT_ACKNOWLEDGED_BY_PROVIDER);

        assertThat(event.isPresent(), is(false));
    }

    @Test
    public void shouldNotFindByMandateIdAndEvent_ifEventIsWrong() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .insert(testContext.getJdbi());

        Optional<DirectDebitEvent> event = directDebitEventDao
                .findByMandateIdAndEvent(testMandate.getId(), MANDATE,
                        MANDATE_FAILED);

        assertThat(event.isPresent(), is(false));
    }
}
