package uk.gov.pay.directdebit.tasks.services;

import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.core.Is.is;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class ExpireResourceTest {

    @DropwizardTestContext
    private TestContext testContext;
    
    private GatewayAccountFixture testGatewayAccount;
    

    @Before
    public void setUp() {
        testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
    }

    @Test
    public void shouldExpireATransactionInStateStartedOlderThanOneDay() throws Exception {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount).insert(testContext.getJdbi());
        TransactionFixture.aTransactionFixture()
                .withState(PaymentState.NEW)
                .withCreatedDate(ZonedDateTime.of(2018,1,1,1,1,1,1,ZoneId.systemDefault()))
                .withMandateFixture(mandateFixture)
                .insert(testContext.getJdbi());
        String requestPath = "/v1/api/tasks/expire-payments-and-mandates";
        ValidatableResponse response = given()
                .port(testContext.getPort())
                .contentType(JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("numberOfExpiredPayments", is(1));
    }


}
