package uk.gov.pay.directdebit.tasks.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class ExpireResourceIT {

    @DropwizardTestContext
    private TestContext testContext;
    
    private GatewayAccountFixture testGatewayAccount;

    @Before
    public void setUp() {
        testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
    }

    @Test
    public void shouldExpireATransactionInStateStartedOlderThanOneDay() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount).insert(testContext.getJdbi());
        PaymentFixture.aPaymentFixture()
                .withState(PaymentState.NEW)
                .withCreatedDate(ZonedDateTime.of(2018,1,1,1,1,1,1,ZoneId.systemDefault()))
                .withMandateFixture(mandateFixture)
                .insert(testContext.getJdbi());
        String requestPath = "/v1/api/tasks/expire-payments-and-mandates";
        given().port(testContext.getPort())
            .contentType(JSON)
            .post(requestPath)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(JSON)
            .body("numberOfExpiredPayments", is(1));
    }

    @Test
    public void shouldExpireAMandateInState_CREATED_OlderThan_90Minutes() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(MandateState.CREATED)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi()).toEntity();
        
        String requestPath = "/v1/api/tasks/expire-payments-and-mandates";
        given()
            .port(testContext.getPort())
            .contentType(JSON)
            .post(requestPath)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(JSON)
            .body("numberOfExpiredMandates", is(1));

        MandateDao mandateDao = testContext.getJdbi().onDemand(MandateDao.class);
        assertEquals(MandateState.EXPIRED, mandateDao.findById(mandate.getId()).get().getState());
    }
}
