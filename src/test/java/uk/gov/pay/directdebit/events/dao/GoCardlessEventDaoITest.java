package uk.gov.pay.directdebit.events.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;

import java.sql.Timestamp;
import java.util.Map;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GoCardlessEventDaoITest {

    @DropwizardTestContext
    private TestContext testContext;

    private GoCardlessEventDao goCardlessEventDao;

    @Before
    public void setup() {
        goCardlessEventDao = testContext.getJdbi().onDemand(GoCardlessEventDao.class);
    }

    @Test
    public void shouldInsertAnEvent() {
        GoCardlessEventFixture goCardlessEventFixture = GoCardlessEventFixture.aGoCardlessEventFixture();

        Long id = goCardlessEventDao.insert(goCardlessEventFixture.toEntity());

        Map<String, Object> goCardlessEvent = testContext.getDatabaseTestHelper().getGoCardlessEventById(id);

        assertThat(goCardlessEvent.get("id"), is(id));
        assertThat(goCardlessEvent.get("internal_event_id"), is(nullValue()));
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

}
