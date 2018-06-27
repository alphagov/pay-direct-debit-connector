package uk.gov.pay.directdebit.payments.resources;

import org.junit.Before;
import org.junit.Test;
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

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture.aDirectDebitEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_ACKNOWLEDGED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.MANDATE;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GetDirectDebitEventsTest {

    @DropwizardTestContext
    private TestContext testContext;

    private MandateFixture testMandate;
    private TransactionFixture testTransaction;
    private GatewayAccountFixture gatewayAccountFixture;

    @Before
    public void setUp() {
        gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());
        this.testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
        this.testTransaction = TransactionFixture.aTransactionFixture().withMandateFixture(testMandate).insert(testContext.getJdbi());
    }

    @Test
    public void shouldReturnAllEventsForNoSearchParameters() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());
        
        String requestPath = "/v1/events";

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("$", hasSize(1));
    }
    
    @Test
    public void shouldReturnBadRequestIfDatesAreNotValid() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?before=%s", "invalid_format");

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
    
    @Test
    public void shouldReturnAnEventWithAllSearchParameters() {
        DirectDebitEventFixture directDebitEventFixture = aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?before=%s&after=%s&page_size=100&page=1&mandate_id=%s&transaction_id=%s",
                ZonedDateTime.now().plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT),
                ZonedDateTime.now().minusMinutes(5).format(DateTimeFormatter.ISO_INSTANT),
                testMandate.getId().toString(),
                testTransaction.getId().toString());
        
        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("$", hasSize(1))
                .body("[0].mandate_id", is(Math.toIntExact(testMandate.getId())))
                .body("[0].transaction_id", is(Math.toIntExact(testTransaction.getId())))
                .body("[0].event_type", is(MANDATE.toString()))
                .body("[0].event", is(PAYMENT_ACKNOWLEDGED_BY_PROVIDER.toString()))
                .body("[0].event_date", is(directDebitEventFixture.getEventDate().format(DateTimeFormatter.ISO_INSTANT).toString()))
        ;
    }

    @Test
    public void shouldReturnNoEventsForNonExistentDates() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?before=%s&after=%s&page_size=100&page=1&mandate_id=%s&transaction_id=%s",
                ZonedDateTime.now().minusMinutes(5).format(DateTimeFormatter.ISO_INSTANT),
                ZonedDateTime.now().minusMinutes(5).format(DateTimeFormatter.ISO_INSTANT),
                testMandate.getId().toString(),
                testTransaction.getId().toString());

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("$", hasSize(0));
    }

    @Test
    public void shouldReturnAnEventForBeforeParameter() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?before=%s",
                ZonedDateTime.now().plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT));

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("$", hasSize(1));
    }

    @Test
    public void shouldReturnAnEventForAfterParameter() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?after=%s",
                ZonedDateTime.now().minusMinutes(5).format(DateTimeFormatter.ISO_INSTANT));

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("$", hasSize(1));
    }

    @Test
    public void shouldReturnAnEventForMandateIdParameter() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?mandate_id=%s", testMandate.getId());

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("$", hasSize(1));
    }

    @Test
    public void shouldReturnAnEventForTransactionIdParameter() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?transaction_id=%s", testTransaction.getId());

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("$", hasSize(1));
    }
    
    @Test
    public void shouldReturnTwoEventsWherePageSizeIsSetToTwo() {
        for (int i = 0; i < 4; i++) {
            aDirectDebitEventFixture()
                    .withMandateId(testMandate.getId())
                    .withTransactionId(testTransaction.getId())
                    .withEventType(MANDATE)
                    .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                    .withEventDate(ZonedDateTime.now())
                    .insert(testContext.getJdbi());
        }

        String requestPath = format("/v1/events?transaction_id=%s&page_size=2", testTransaction.getId());

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("$", hasSize(2));
    }
    
    @Test
    public void shouldReturnBadRequestIfPageIsSetAndPageSizeIsNotSet() {
        
    }
    
    @Test
    public void shouldReturnThirdEventWherePageSizeIsTwoAndPageIsTwo() {
        for (int i = 1; i < 4; i++) {
            aDirectDebitEventFixture()
                    .withMandateId(testMandate.getId())
                    .withTransactionId(testTransaction.getId())
                    .withEventType(MANDATE)
                    .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                    .withEventDate(ZonedDateTime.now())
                    .insert(testContext.getJdbi());
        }

        String requestPath = format("/v1/events?transaction_id=%s&page_size=2&page=2", testTransaction.getId());

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("$", hasSize(1));
    }
    
    @Test
    public void shouldReturnFiveHundredEventsWhenPageSizeIsFiveHundredAndOne() {
        
    }
}
