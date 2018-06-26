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
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.hamcrest.Matchers.hasSize;
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

    @Before
    public void setUp() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());
        this.testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
        this.testTransaction = TransactionFixture.aTransactionFixture().withMandateFixture(testMandate).insert(testContext.getJdbi());
    }

    @Test
    public void shouldReturnNoEventsForNoSearchParameters() {
        
    }
    
    @Test
    public void shouldThrow422IfDatesAreNotValid() {
        
    }
    
    @Test
    public void shouldReturnAnEventWithAllSearchParameters() {
        aDirectDebitEventFixture()
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
                .body("results", hasSize(1));
    }

    @Test
    public void shouldReturnNoEventsForNonExistentDates() {

    }

    @Test
    public void shouldReturnAnEventForBeforeParameter() {

    }

    @Test
    public void shouldReturnAnEventForAfterParameter() {

    }

    @Test
    public void shouldReturnAnEventForMandateIdParameter() {

    }

    @Test
    public void shouldReturnAnEventForTransactionIdParameter() {

    }
    
    @Test
    public void shouldReturnTwoEventsWherePageSizeIsSetToTwo() {
        
    }
    
    @Test
    public void shouldReturnThirdEventWherePageSizeIsTwoAndPageIsTwo() {
        
    }
    
    @Test
    public void shouldReturnFiveHundredEventsWhenPageSizeIsFiveHundredAndOne() {
        
    }
}
