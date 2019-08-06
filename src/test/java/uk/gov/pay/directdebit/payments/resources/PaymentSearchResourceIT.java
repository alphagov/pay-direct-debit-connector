package uk.gov.pay.directdebit.payments.resources;

import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Rule;
import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.directdebit.junit.DropwizardAppWithPostgresRule;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;

public class PaymentSearchResourceIT {

    private GatewayAccountFixture testGatewayAccount;
    private TestContext testContext;

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
    public void searchPaymentsWithAllParameters() {
        MandateFixture mandateFixture = aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .withExternalId(MandateExternalId.valueOf("abc"))
                .insert(testContext.getJdbi());
        
        for (int i = 0; i < 3; i++) {
            aPaymentFixture()
                    .withId((long) i)
                    .withAmount(100L)
                    .withMandateFixture(mandateFixture)
                    .withReference("MBK" + i)
                    .withDescription("Description" + i)
                    .withCreatedDate(ZonedDateTime.of(2019, 8, 6, 10, 0, 0, 0, UTC))
                    .withState(PaymentState.CREATED)
                    .insert(testContext.getJdbi());
        }
        
        String requestPath = ("/v1/api/accounts/{accountId}/payments?reference=MBK" +
                "&amount=100" +
                "&mandate_id=abc" +
                "&from_date=2019-08-06T09:00:00.000Z" +
                "&to_date=2019-08-06T10:00:00.001Z" +
                "&state=created")
                .replace("{accountId}", testGatewayAccount.getExternalId());

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("page", is(1))
                .body("total", is(3))
                .body("count", is(3))
                .body("results", hasSize(3))
                .body("results[0].reference", is("MBK2"))
                .body("results[2].description", is("Description0"))
                .body("results[1].created_date", is(ZonedDateTime.of(2019, 8, 6, 10, 0, 0, 0, UTC).format(ISO_INSTANT_MILLISECOND_PRECISION)));
    }

    @Test
    public void shouldReturn400_whenPresentedWithNegativeOffset() {
        String requestPath = "/v1/api/accounts/{accountId}/payments?page=:page&display_size=:display_size"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":page", "0")
                .replace(":display_size", "100");

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(422)
                .contentType(JSON)
                .body("message", contains("Invalid attribute value: page. Must be greater than or equal to 1"))
                .body("error_identifier", is(ErrorIdentifier.GENERIC.toString()));
    }

    @Test
    public void shouldReturn404_whenGatewayAccountNotExists() {
        String requestPath = "/v1/api/accounts/{accountId}/payments?page=:page&display_size=:display_size"
                .replace("{accountId}", "non-existent-id")
                .replace(":page", "1")
                .replace(":display_size", "100");

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .contentType(JSON);
    }

    @Test
    public void shouldReturnEmptyResults_whenPaginationFindsNoRecords() {
        for (int i = 0; i < 2; i++) {
            MandateFixture mandateFixture = aMandateFixture()
                    .withGatewayAccountFixture(testGatewayAccount)
                    .insert(testContext.getJdbi());
            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .insert(testContext.getJdbi());
        }

        String requestPath = "/v1/api/accounts/{accountId}/payments?page=:page&display_size=:display_size"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":page", "2")
                .replace(":display_size", "2");


        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("results", hasSize(0));
    }

    @Test
    public void shouldReturnPageOfPaginatedResults() {
        for (int i = 0; i < 15; i++) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount).insert(testContext.getJdbi());

            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .insert(testContext.getJdbi());
        }

        String requestPath = "/v1/api/accounts/{accountId}/payments?page=:page&display_size=:display_size"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":page", "2")
                .replace(":display_size", "10");

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("results", hasSize(5))
                .body("total", is(15))
                .body("count", is(5))
                .body("page", is(2));
    }

    @Test
    public void shouldReturn400_whenMalformedDate() {
        String fromDate = "2018-05-05T15:00Z";
        String toDate = "2018-14-08T15:00Z";
        String requestPath = "/v1/api/accounts/{accountId}/payments?page=:page&display_size=:display_size&from_date=:fromDate&to_date=:toDate"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":page", "2")
                .replace(":display_size", "10")
                .replace(":fromDate", fromDate)
                .replace(":toDate", toDate);
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(422)
                .contentType(JSON)
                .body("message", contains("Invalid attribute value: to_date. Must be a valid date"))
                .body("error_identifier", is(ErrorIdentifier.GENERIC.toString()));
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
