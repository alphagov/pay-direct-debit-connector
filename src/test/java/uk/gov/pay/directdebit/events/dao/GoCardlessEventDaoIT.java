package uk.gov.pay.directdebit.events.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GoCardlessEventId;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GoCardlessEventDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private GoCardlessEventDao goCardlessEventDao;

    @Before
    public void setup() {
        goCardlessEventDao = testContext.getJdbi().onDemand(GoCardlessEventDao.class);
    }

    @Test
    public void shouldInsertAnEvent() {
        GoCardlessEventFixture goCardlessEventFixture = aGoCardlessEventFixture();

        Long id = goCardlessEventDao.insert(goCardlessEventFixture.toEntity());

        Map<String, Object> goCardlessEvent = testContext.getDatabaseTestHelper().getGoCardlessEventById(id);

        assertThat(goCardlessEvent.get("id"), is(id));
        assertThat(goCardlessEvent.get("event_id"), is(goCardlessEventFixture.getGoCardlessEventId().toString()));
        assertThat(goCardlessEvent.get("action"), is(goCardlessEventFixture.getAction()));
        assertThat(goCardlessEvent.get("resource_type"), is(goCardlessEventFixture.getResourceType().toString()));
        assertThat(goCardlessEvent.get("json").toString(), is(goCardlessEventFixture.getJson()));
        assertThat(goCardlessEvent.get("details_cause"), is(goCardlessEventFixture.getDetailsCause()));
        assertThat(goCardlessEvent.get("details_description"), is(goCardlessEventFixture.getDetailsDescription()));
        assertThat(goCardlessEvent.get("details_origin"), is(goCardlessEventFixture.getDetailsOrigin()));
        assertThat(goCardlessEvent.get("details_reason_code"), is(goCardlessEventFixture.getDetailsReasonCode()));
        assertThat(goCardlessEvent.get("details_scheme"), is(goCardlessEventFixture.getDetailsScheme()));
        assertThat(goCardlessEvent.get("links_mandate"), is(goCardlessEventFixture.getLinksMandate().toString()));
        assertThat(goCardlessEvent.get("links_new_customer_bank_account"), is(goCardlessEventFixture.getLinksNewCustomerBankAccount()));
        assertThat(goCardlessEvent.get("links_new_mandate"), is(goCardlessEventFixture.getLinksNewMandate().toString()));
        assertThat(goCardlessEvent.get("links_organisation"), is(goCardlessEventFixture.getLinksOrganisation().toString()));
        assertThat(goCardlessEvent.get("links_parent_event"), is(goCardlessEventFixture.getLinksParentEvent()));
        assertThat(goCardlessEvent.get("links_payment"), is(goCardlessEventFixture.getLinksPayment().toString()));
        assertThat(goCardlessEvent.get("links_payout"), is(goCardlessEventFixture.getLinksPayout()));
        assertThat(goCardlessEvent.get("links_previous_customer_bank_account"), is(goCardlessEventFixture.getLinksPreviousCustomerBankAccount()));
        assertThat(goCardlessEvent.get("links_refund"), is(goCardlessEventFixture.getLinksRefund()));
        assertThat(goCardlessEvent.get("links_subscription"), is(goCardlessEventFixture.getLinksSubscription()));
        assertThat(goCardlessEvent.get("created_at"), is(Timestamp.from(goCardlessEventFixture.getCreatedAt().toInstant())));
    }

    @Test
    public void shouldFindLatestApplicableEventForMandate() {
        GoCardlessEventFixture latestEvent = aGoCardlessEventFixture().withLinksMandate(GoCardlessMandateId.valueOf("Mandate ID we want"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("Organisation ID we want"))
                .withAction("Action we want")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 5, 12, 0, 0, 0, UTC))
                .withGoCardlessEventId(GoCardlessEventId.valueOf("This is the latest applicable event"));

        GoCardlessEventFixture earlierEvent = aGoCardlessEventFixture().withLinksMandate(GoCardlessMandateId.valueOf("Mandate ID we want"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("Organisation ID we want"))
                .withAction("Action we want")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 5, 11, 59, 59, 999_999_999, UTC))
                .withGoCardlessEventId(GoCardlessEventId.valueOf("This is an earlier event"));

        GoCardlessEventFixture laterEventWrongAction = aGoCardlessEventFixture().withLinksMandate(GoCardlessMandateId.valueOf("Mandate ID we want"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("Organisation ID we want"))
                .withAction("Different action")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 5, 13, 0, 0, 0, UTC))
                .withGoCardlessEventId(GoCardlessEventId.valueOf("This is a later event but with the wrong action"));

        GoCardlessEventFixture laterEventWrongMandateId = aGoCardlessEventFixture().withLinksMandate(GoCardlessMandateId.valueOf("Different mandate ID"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("Organisation ID we want"))
                .withAction("Action we want")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 5, 13, 0, 0, 0, UTC))
                .withGoCardlessEventId(GoCardlessEventId.valueOf("This is an later event but with the wrong mandate ID"));

        GoCardlessEventFixture laterEventWrongOrganisationId = aGoCardlessEventFixture().withLinksMandate(GoCardlessMandateId.valueOf("Mandate ID we want"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("Different organisation ID"))
                .withAction("Action we want")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 5, 13, 0, 0, 0, UTC))
                .withGoCardlessEventId(GoCardlessEventId.valueOf("This is an later event with the wrong organisation ID"));

        goCardlessEventDao.insert(latestEvent.toEntity());
        goCardlessEventDao.insert(earlierEvent.toEntity());
        goCardlessEventDao.insert(laterEventWrongAction.toEntity());
        goCardlessEventDao.insert(laterEventWrongMandateId.toEntity());
        goCardlessEventDao.insert(laterEventWrongOrganisationId.toEntity());

        GoCardlessEvent event = goCardlessEventDao.findLatestApplicableEventForMandate(
                GoCardlessMandateId.valueOf("Mandate ID we want"),
                GoCardlessOrganisationId.valueOf("Organisation ID we want"),
                Set.of("Action we want")).get();

        assertThat(event.getGoCardlessEventId(), is(GoCardlessEventId.valueOf("This is the latest applicable event")));
    }

    @Test
    public void shouldFindLatestApplicableEventForPayment() {
        GoCardlessEventFixture latestEvent = aGoCardlessEventFixture().withLinksPayment(GoCardlessPaymentId.valueOf("Payment ID we want"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("Organisation ID we want"))
                .withAction("Action we want")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 5, 12, 0, 0, 0, UTC))
                .withGoCardlessEventId(GoCardlessEventId.valueOf("This is the latest applicable event"));

        GoCardlessEventFixture earlierEvent = aGoCardlessEventFixture().withLinksPayment(GoCardlessPaymentId.valueOf("Payment ID we want"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("Organisation ID we want"))
                .withAction("Action we want")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 5, 11, 59, 59, 999_999_999, UTC))
                .withGoCardlessEventId(GoCardlessEventId.valueOf("This is an earlier event"));

        GoCardlessEventFixture laterEventWrongAction = aGoCardlessEventFixture().withLinksPayment(GoCardlessPaymentId.valueOf("Payment ID we want"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("Organisation ID we want"))
                .withAction("Different action")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 5, 13, 0, 0, 0, UTC))
                .withGoCardlessEventId(GoCardlessEventId.valueOf("This is a later event but with the wrong action"));

        GoCardlessEventFixture laterEventWrongPaymentId = aGoCardlessEventFixture().withLinksPayment(GoCardlessPaymentId.valueOf("Different payment ID"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("Organisation ID we want"))
                .withAction("Action we want")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 5, 13, 0, 0, 0, UTC))
                .withGoCardlessEventId(GoCardlessEventId.valueOf("This is an later event but with the wrong payment ID"));

        GoCardlessEventFixture laterEventWrongOrganisationId = aGoCardlessEventFixture().withLinksPayment(GoCardlessPaymentId.valueOf("Payment ID we want"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("Different organisation ID"))
                .withAction("Action we want")
                .withCreatedAt(ZonedDateTime.of(2019, 7, 5, 13, 0, 0, 0, UTC))
                .withGoCardlessEventId(GoCardlessEventId.valueOf("This is an later event with the wrong organisation ID"));

        goCardlessEventDao.insert(latestEvent.toEntity());
        goCardlessEventDao.insert(earlierEvent.toEntity());
        goCardlessEventDao.insert(laterEventWrongAction.toEntity());
        goCardlessEventDao.insert(laterEventWrongPaymentId.toEntity());
        goCardlessEventDao.insert(laterEventWrongOrganisationId.toEntity());

        GoCardlessEvent event = goCardlessEventDao.findLatestApplicableEventForPayment(
                GoCardlessPaymentId.valueOf("Payment ID we want"),
                GoCardlessOrganisationId.valueOf("Organisation ID we want"),
                Set.of("Action we want")).get();

        assertThat(event.getGoCardlessEventId(), is(GoCardlessEventId.valueOf("This is the latest applicable event")));
    }

    @Test
    public void shouldNotFindAnythingIfNoApplicableEventForMandate() {
        Optional<GoCardlessEvent> event = goCardlessEventDao.findLatestApplicableEventForMandate(
                GoCardlessMandateId.valueOf("Mandate ID we want"),
                GoCardlessOrganisationId.valueOf("Organisation ID we want"),
                Set.of("Action we want"));

        assertThat(event, is(Optional.empty()));
    }

}
