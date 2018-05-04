package uk.gov.pay.directdebit.payments.resources;

import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;

import javax.ws.rs.core.Response;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentViewResourceTest {

    private GatewayAccountFixture testGatewayAccount;

    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setUp() {
        testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
    }

    @Test
    public void shouldReturnAListOfPaymentView() {
        ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);
        for (int i = 0; i < 3; i++) {
            PaymentRequestFixture paymentRequestFixture = aPaymentRequestFixture()
                    .withId(i + 1)
                    .withGatewayAccountId(testGatewayAccount.getId())
                    .withReference("MBK" + i)
                    .withDescription("Description" + i)
                    .withCreatedDate(createdDate)
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .withName("J. Doe" + i)
                    .insert(testContext.getJdbi());
        }
        String requestPath = "/v1/api/accounts/{accountId}/view?page=:page&display_size=:display_size"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":page", "1")
                .replace(":display_size", "100");

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("gateway_account_external_id", is(testGatewayAccount.getExternalId()))
                .body("page", is(1))
                .body("display_size", is(100))
                .body("payment_views", hasSize(3))
                .body("payment_views[0].reference", is("MBK2"))
                .body("payment_views[2].description", is("Description0"))
                .body("gateway_account_external_id", is(testGatewayAccount.getExternalId()))
                .body("payment_views[0].name", is("J. Doe2"))
                .body("payment_views[1].created_date", is(createdDate.toString()));
    }

    @Test
    public void shouldReturn405_whenPresentedWithNegativeOffset() {
        String requestPath = "/v1/api/accounts/{accountId}/view?page=:page&display_size=:display_size"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":page", "0")
                .replace(":display_size", "100");

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Query param 'page' should be a non zero positive integer"));
    }

    @Test
    public void shouldReturn404_whenGatewayAccountNotExists() {
        String requestPath = "/v1/api/accounts/{accountId}/view?page=:page&display_size=:display_size"
                .replace("{accountId}", "non-existent-id")
                .replace(":page", "1")
                .replace(":display_size", "100");

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .contentType(JSON)
                .body("message", is("Unknown gateway account: non-existent-id"));
    }

    @Test
    public void shouldReturn404_whenPaginationFindsNoRecords() {
        for (int i = 0; i < 2; i++) {
            PaymentRequestFixture paymentRequestFixture = aPaymentRequestFixture()
                    .withGatewayAccountId(testGatewayAccount.getId())
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .insert(testContext.getJdbi());
        }

        String requestPath = "/v1/api/accounts/{accountId}/view?page=:page&display_size=:display_size"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":page", "2")
                .replace(":display_size", "2");


        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .contentType(JSON)
                .body("message", is("Found no records with page size 2 and display_size 2"));
    }

    @Test
    public void shouldReturn2Records_whenPaginationSetToPage2AndDisplaySize3With5records() {
        ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);
        for (int i = 0; i < 5; i++) {
            PaymentRequestFixture paymentRequestFixture = aPaymentRequestFixture()
                    .withId(i + 1)
                    .withGatewayAccountId(testGatewayAccount.getId())
                    .withReference("MBK" + i)
                    .withDescription("Description" + i)
                    .withCreatedDate(createdDate)
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .withName("J. Doe" + i)
                    .insert(testContext.getJdbi());
        }
        String requestPath = "/v1/api/accounts/{accountId}/view?page=:page&display_size=:display_size"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":page", "2")
                .replace(":display_size", "3");

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("payment_views", hasSize(2))
                .body("gateway_account_external_id", is(testGatewayAccount.getExternalId()))
                .body("page", is(2))
                .body("display_size", is(3))
                .body("payment_views[0].reference", is("MBK1"))
                .body("payment_views[1].description", is("Description0"))
                .body("payment_views[0].name", is("J. Doe1"));
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
