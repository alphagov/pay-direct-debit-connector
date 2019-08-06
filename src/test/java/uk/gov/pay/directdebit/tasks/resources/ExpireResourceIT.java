package uk.gov.pay.directdebit.tasks.resources;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Rule;
import uk.gov.pay.directdebit.junit.DropwizardAppWithPostgresRule;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.payments.fixtures.GovUkPayEventFixture.aGovUkPayEventFixture;

public class ExpireResourceIT {

    private TestContext testContext;
    
    private GatewayAccountFixture testGatewayAccount;

    @Rule
    public DropwizardAppWithPostgresRule app = new DropwizardAppWithPostgresRule();

    @Before
    public void setUp() {
        testContext = app.getTestContext();
        testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
    }

    @After
    public void tearDown() {
        app.getDatabaseTestHelper().truncateAllData();
    }

    @Test
    public void shouldExpireAMandateInState_CREATED_OlderThan_90Minutes() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(MandateState.CREATED)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi()).toEntity();

        aGovUkPayEventFixture()
                .withMandateId(mandate.getId())
                .withEventType(MANDATE_CREATED)
                .insert(testContext.getJdbi());
        
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
        assertEquals(MandateState.USER_SETUP_EXPIRED, mandateDao.findById(mandate.getId()).get().getState());
    }
}
