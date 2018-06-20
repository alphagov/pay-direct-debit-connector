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
import uk.gov.pay.directdebit.tasks.resources.ExpireResource;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class ExpireServiceTest {

    private static final String FRONTEND_CARD_DETAILS_URL = "/secure";
    private static final String JSON_AMOUNT_KEY = "amount";
    private static final String JSON_REFERENCE_KEY = "reference";
    private static final String JSON_DESCRIPTION_KEY = "description";
    private static final String JSON_GATEWAY_ACC_KEY = "gateway_account_id";
    private static final String JSON_RETURN_URL_KEY = "return_url";
    private static final String JSON_CHARGE_KEY = "charge_id";
    private static final String JSON_STATE_KEY = "state.status";
    private static final long AMOUNT = 6234L;
    private GatewayAccountFixture testGatewayAccount;
    
    @DropwizardTestContext
    private TestContext testContext;
    

    @Before
    public void setUp() {
        testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
    }

    @Test
    public void shouldExpireATransactionInStateStartedOlderThanOneDay() throws Exception {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
        MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
        TransactionFixture.aTransactionFixture()
                .withState(PaymentState.NEW)
                .withCreatedDate(ZonedDateTime.of(2018,1,1,1,1,1,1,ZoneId.systemDefault()))
                .withMandateFixture(mandateFixture)
                .insert(testContext.getJdbi());
        String requestPath = "/v1/api/tasks/expirePaymentsAndMandates";
        ValidatableResponse response = given()
                .port(testContext.getPort())
                .contentType(JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }


}
