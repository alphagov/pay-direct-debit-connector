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
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentViewResourceITest {

    private GatewayAccountFixture testGatewayAccount;

    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setUp() {
        testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
    }

    @Test
    public void shouldReturnAListOfPaymentView() {
        ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1L);
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
    public void shouldReturn400_whenPresentedWithNegativeOffset() {
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
    public void shouldReturn25Records_whenPaginationSetToPage2AndDisplaySize3With5records_withNoDates() {
        ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1L);
        for (int i = 0; i < 50; i++) {
            PaymentRequestFixture paymentRequestFixture = aPaymentRequestFixture()
                    .withAmount(100 + i)
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
                .replace(":display_size", "25");
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("payment_views", hasSize(25))
                .body("gateway_account_external_id", is(testGatewayAccount.getExternalId()))
                .body("page", is(2))
                .body("display_size", is(25))
                .body("payment_views[0].reference", is("MBK24"))
                .body("payment_views[24].description", is("Description0"))
                .body("payment_views[0].name", is("J. Doe24"));
    }

    @Test
    public void shouldReturn5Records_whenPaginationSetTo2PageAnd10DisplaySizeWith15records_withDateRange10days() {
        ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1L);
        for (int i = 0; i < 15; i++) {
            PaymentRequestFixture paymentRequestFixture = aPaymentRequestFixture()
                    .withAmount(100 + i)
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

        String requestPath = "/v1/api/accounts/{accountId}/view?page=:page&display_size=:display_size&from_date=:fromDate&to_date=:toDate"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":page", "2")
                .replace(":display_size", "10")
                .replace(":fromDate", ZonedDateTime.now(ZoneOffset.UTC).minusDays(10L).toString())
                .replace(":toDate", ZonedDateTime.now(ZoneOffset.UTC).toString());
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("payment_views", hasSize(5))
                .body("gateway_account_external_id", is(testGatewayAccount.getExternalId()))
                .body("page", is(2))
                .body("display_size", is(10))
                .body("payment_views[0].reference", is("MBK4"))
                .body("payment_views[4].description", is("Description0"))
                .body("payment_views[1].name", is("J. Doe3"))
                .body("payment_views[2].created_date", is(createdDate.toString()));
    }

    @Test
    public void shouldReturn3Records_whenFromAndToDateSet() {
        ZonedDateTime fromDate = ZonedDateTime.now(ZoneOffset.UTC).minusYears(1).minusDays(4L).minusMinutes(5L);
        ZonedDateTime toDate = ZonedDateTime.now(ZoneOffset.UTC).minusYears(1).minusDays(2L).plusMinutes(5L);
        for (int i = 0, day = 15; i < 15; i++, day--) {
            PaymentRequestFixture paymentRequestFixture = aPaymentRequestFixture()
                    .withAmount(100 + i)
                    .withId(i + 1)
                    .withGatewayAccountId(testGatewayAccount.getId())
                    .withReference("MBK" + day)
                    .withDescription("Description" + i)
                    .withCreatedDate(ZonedDateTime.now(ZoneOffset.UTC).minusYears(1).minusDays(day))
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .withName("J. Doe" + day)
                    .insert(testContext.getJdbi());
        }

        String requestPath = "/v1/api/accounts/{accountId}/view?page=:page&display_size=:display_size&from_date=:fromDate&to_date=:toDate"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":page", "1")
                .replace(":display_size", "100")
                .replace(":fromDate", fromDate.toString())
                .replace(":toDate", toDate.toString());
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("payment_views", hasSize(3))
                .body("gateway_account_external_id", is(testGatewayAccount.getExternalId()))
                .body("page", is(1))
                .body("display_size", is(100))
                .body("payment_views[0].reference", is("MBK2"))
                .body("payment_views[2].description", is("Description11"))
                .body("payment_views[1].name", is("J. Doe3"));
    }

    @Test
    public void shouldReturn400_whenMalformedDate() {
        String fromDate = "2018-05-05T15:00Z";
        String toDate = "2018-14-08T15:00Z";
        String requestPath = "/v1/api/accounts/{accountId}/view?page=:page&display_size=:display_size&from_date=:fromDate&to_date=:toDate"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":page", "2")
                .replace(":display_size", "10")
                .replace(":fromDate", fromDate)
                .replace(":toDate", toDate);
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Input toDate (2018-14-08T15:00Z) is wrong format"));
    }

    @Test
    public void shouldReturn7Records_whenEmailSet() {
        List<String> emailList = Arrays.asList("john.doe@example.com", "jane.doe@example.com");
        for (int i = 0; i < 15; i++) {
            PaymentRequestFixture paymentRequestFixture = aPaymentRequestFixture()
                    .withGatewayAccountId(testGatewayAccount.getId())
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .withEmail(i % 2 == 0 ? emailList.get(0) : emailList.get(1))
                    .insert(testContext.getJdbi());
        }

        String requestPath = "/v1/api/accounts/{accountId}/view?email=:email"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":email", "Jane");
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("payment_views", hasSize(7));
    }

    @Test
    public void shouldReturn5Records_whenReferenceSet_ignoreCase() {
        List<String> referenceList = Arrays.asList("REF1", "REF2");
        for (int i = 0; i < 15; i++) {
            PaymentRequestFixture paymentRequestFixture = aPaymentRequestFixture()
                    .withGatewayAccountId(testGatewayAccount.getId())
                    .withReference(i % 3 == 0 ? referenceList.get(0) : referenceList.get(1))
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .insert(testContext.getJdbi());
        }

        String requestPath = "/v1/api/accounts/{accountId}/view?reference=:reference"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":reference", "f1");
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("payment_views", hasSize(5));
    }

    @Test
    public void shouldReturn6Records_whenAmountSet() {
        List<Long> amountList = Arrays.asList(1051L, 2343L, 1111L);
        for (int i = 0; i < 20; i++) {
            PaymentRequestFixture paymentRequestFixture = aPaymentRequestFixture()
                    .withGatewayAccountId(testGatewayAccount.getId())
                    .withAmount(i % 3 == 0 ? amountList.get(0) : i % 2 == 0 ? amountList.get(1) : amountList.get(2))
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withPaymentRequestId(paymentRequestFixture.getId())
                    .insert(testContext.getJdbi());
        }

        String requestPath = "/v1/api/accounts/{accountId}/view?amount=:amount"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":amount", "2343");
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("payment_views", hasSize(6));
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
