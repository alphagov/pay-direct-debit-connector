package uk.gov.pay.directdebit.payments.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEventId;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture.aDirectDebitEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.PAYMENTS;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GoCardlessDirectDebitDirectDebitEventDaoIT {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private GoCardlessEventDao goCardlessEventDao;

    @DropwizardTestContext
    private TestContext testContext;

    private final static Long EVENT_ID = 6L;
    private final static GoCardlessEventId GOCARDLESS_EVENT_ID = GoCardlessEventId.valueOf("dhg2342h3kjh");
    private final static String GOCARDLESS_ACTION = "something happened";
    private final static GoCardlessResourceType GOCARDLESS_RESOURCE_TYPE = PAYMENTS;
    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode eventJson;
    private final static String DETAILS_CAUSE  = "a detail cause";
    private final static String DETAILS_DESCRIPTION = "a detail description";
    private final static String DETAILS_ORIGIN  = "a detail origin";
    private final static String DETAILS_REASONCODE = "a detail reason code";
    private final static String DETAILS_SCHEME  = "a detail scheme";
    private final static String LINKS_MANDATE = "123456";
    private final static String LINKS_NEWCUSTOMERBANKACCOUNT  = "a new customer bank account";
    private final static String LINKS_NEWMANDATE = "a new mandate";
    private final static String LINKS_ORGANISATION  = "an organisation";
    private final static String LINKS_PARENTEVENT = "a parent event";
    private final static String LINKS_PAYMENT  = "a payment";
    private final static String LINKS_PAYOUT = "a payout";
    private final static String LINKS_PREVIOUSCUSTOMERBANKACCOUNT  = "a previous customer bank account";
    private final static String LINKS_REFUND = "a refund";
    private final static String LINKS_SUBSCRIPTION = "a subscription";
    
    private static final ZonedDateTime CREATED_AT = ZonedDateTime.parse("2017-12-30T12:30:40Z");

    private GoCardlessEvent goCardlessEvent;
    private MandateFixture testMandate;
    private GatewayAccountFixture gatewayAccountFixture;

    @Before
    public void setup() throws IOException {
        eventJson = objectMapper.readTree("{\"id\": \"somejson\"}");
        goCardlessEventDao = testContext.getJdbi().onDemand(GoCardlessEventDao.class);
        gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());
        testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withId(EVENT_ID)
                .insert(testContext.getJdbi());
        goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture()
                .withEventId(EVENT_ID)
                .withGoCardlessEventId(GOCARDLESS_EVENT_ID)
                .withAction(GOCARDLESS_ACTION)
                .withResourceType(GOCARDLESS_RESOURCE_TYPE)
                .withDetailsCause(DETAILS_CAUSE)
                .withDetailsDescription(DETAILS_DESCRIPTION)
                .withDetailsOrigin(DETAILS_ORIGIN)
                .withDetailsReasonCode(DETAILS_REASONCODE)
                .withDetailsScheme(DETAILS_SCHEME)
                .withLinksMandate(LINKS_MANDATE)
                .withLinksNewCustomerBankAccount(LINKS_NEWCUSTOMERBANKACCOUNT)
                .withLinksNewMandate(LINKS_NEWMANDATE)
                .withLinksOrganisation(LINKS_ORGANISATION)
                .withLinksParentEvent(LINKS_PARENTEVENT)
                .withLinksPayment(LINKS_PAYMENT)
                .withLinksPayout(LINKS_PAYOUT)
                .withLinksPreviousCustomerBankAccount(LINKS_PREVIOUSCUSTOMERBANKACCOUNT)
                .withLinksRefund(LINKS_REFUND)
                .withLinksSubscription(LINKS_SUBSCRIPTION)
                .withCreatedAt(CREATED_AT)
                .withJson(eventJson.toString())
                .toEntity();
    }

    @Test
    public void shouldInsertAnEvent() throws IOException {
        Long id = goCardlessEventDao.insert(goCardlessEvent);
        Map<String, Object> foundGoCardlessEvent = testContext.getDatabaseTestHelper().getGoCardlessEventById(id);
        assertThat(foundGoCardlessEvent.get("id"), is(id));
        assertThat(foundGoCardlessEvent.get("internal_event_id"), is(EVENT_ID));
        assertThat(foundGoCardlessEvent.get("event_id"), is(GOCARDLESS_EVENT_ID.toString()));
        assertThat(foundGoCardlessEvent.get("action"), is(GOCARDLESS_ACTION));
        assertThat(foundGoCardlessEvent.get("resource_type"), is(GOCARDLESS_RESOURCE_TYPE.toString()));
        assertThat(foundGoCardlessEvent.get("details_cause"), is(DETAILS_CAUSE));
        assertThat(foundGoCardlessEvent.get("details_description"), is(DETAILS_DESCRIPTION));
        assertThat(foundGoCardlessEvent.get("details_origin"), is(DETAILS_ORIGIN));
        assertThat(foundGoCardlessEvent.get("details_reason_code"), is(DETAILS_REASONCODE));
        assertThat(foundGoCardlessEvent.get("details_scheme"), is(DETAILS_SCHEME));
        assertThat(foundGoCardlessEvent.get("links_mandate"), is(LINKS_MANDATE));
        assertThat(foundGoCardlessEvent.get("links_new_customer_bank_account"), is(LINKS_NEWCUSTOMERBANKACCOUNT));
        assertThat(foundGoCardlessEvent.get("links_new_mandate"), is(LINKS_NEWMANDATE));
        assertThat(foundGoCardlessEvent.get("links_organisation"), is(LINKS_ORGANISATION));
        assertThat(foundGoCardlessEvent.get("links_parent_event"), is(LINKS_PARENTEVENT));
        assertThat(foundGoCardlessEvent.get("links_payment"), is(LINKS_PAYMENT));
        assertThat(foundGoCardlessEvent.get("links_payout"), is(LINKS_PAYOUT));
        assertThat(foundGoCardlessEvent.get("links_previous_customer_bank_account"),
                is(LINKS_PREVIOUSCUSTOMERBANKACCOUNT));
        assertThat(foundGoCardlessEvent.get("links_refund"), is(LINKS_REFUND));
        assertThat(foundGoCardlessEvent.get("links_subscription"), is(LINKS_SUBSCRIPTION));
        assertThat(objectMapper.readTree(foundGoCardlessEvent.get("json").toString()), is(eventJson));
        assertThat((Timestamp) foundGoCardlessEvent.get("created_at"), isDate(CREATED_AT));
    }

    @Test
    public void shouldUpdateEventIdAndReturnNumberOfAffectedRows() throws IOException {
        Long newEventId = 10L;
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withId(newEventId)
                .insert(testContext.getJdbi());
        Long id = goCardlessEventDao.insert(goCardlessEvent);
        int numOfUpdatedEvents = goCardlessEventDao.updateEventId(id, newEventId);
        Map<String, Object> eventAfterUpdate = testContext.getDatabaseTestHelper().getGoCardlessEventById(id);
        assertThat(numOfUpdatedEvents, is(1));
        assertThat(eventAfterUpdate.get("internal_event_id"), is(newEventId));
        assertThat(eventAfterUpdate.get("event_id"), is(GOCARDLESS_EVENT_ID.toString()));
        assertThat(eventAfterUpdate.get("action"), is(GOCARDLESS_ACTION));
        assertThat(eventAfterUpdate.get("resource_type"), is(GOCARDLESS_RESOURCE_TYPE.toString()));
        assertThat(objectMapper.readTree(eventAfterUpdate.get("json").toString()), is(eventJson));
        assertThat((Timestamp) eventAfterUpdate.get("created_at"), isDate(CREATED_AT));
    }


}
