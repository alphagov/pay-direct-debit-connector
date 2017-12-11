package uk.gov.pay.directdebit.payments.dao;

import io.dropwizard.jdbi.OptionalContainerFactory;
import liquibase.exception.LiquibaseException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.infra.PostgresResetRule;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardPortValue;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.junit.DropwizardJUnitRunner.getDbConfig;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentRequestEventDaoIT {

    @Rule
    public PostgresResetRule postgresResetRule = new PostgresResetRule(DropwizardJUnitRunner.getDbConfig());

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private PaymentRequestEventDao paymentRequestEventDao;

    private PaymentRequestFixture testPaymentRequest;

    @DropwizardPortValue
    private int port;

    private DBI jdbi;
    private DatabaseTestHelper databaseTestHelper;

    @Before
    public void setup() throws IOException, LiquibaseException {
        jdbi = new DBI(getDbConfig().getUrl(), getDbConfig().getUser(), getDbConfig().getPassword());
        jdbi.registerContainerFactory(new OptionalContainerFactory());
        databaseTestHelper = new DatabaseTestHelper(jdbi);
        paymentRequestEventDao = jdbi.onDemand(PaymentRequestEventDao.class);
        this.testPaymentRequest = aPaymentRequestFixture()
                .withGatewayAccountId(RandomUtils.nextLong(1, 99999))
                .insert(jdbi);
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
