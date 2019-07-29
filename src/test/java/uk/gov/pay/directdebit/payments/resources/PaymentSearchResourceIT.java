package uk.gov.pay.directdebit.payments.resources;

import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentSearchResourceIT { //TODO merge with PaymentResource

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
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount).insert(testContext.getJdbi());
            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference("MBK" + i)
                    .withDescription("Description" + i)
                    .withCreatedDate(createdDate)
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .withName("J. Doe" + i)
                    .insert(testContext.getJdbi());
        }
        String requestPath = "/v1/api/accounts/{accountId}/payments?page=:page&display_size=:display_size"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":page", "1")
                .replace(":display_size", "100");

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
                .body("results[1].created_date", is(createdDate.format(ISO_INSTANT_MILLISECOND_PRECISION)));
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
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", contains("Query param 'page' should be a non zero positive integer"))
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
                .contentType(JSON)
                .body("message", contains("Unknown gateway account: non-existent-id"))
                .body("error_identifier", is(ErrorIdentifier.GENERIC.toString()));
    }

    @Test
    public void shouldReturnEmptyResults_whenPaginationFindsNoRecords() {
        for (int i = 0; i < 2; i++) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount).insert(testContext.getJdbi());
            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
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
    public void shouldReturn5Records_whenPaginationSetTo2PageAnd10DisplaySizeWith15records_withDateRange10days() {
        ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1L);
        for (int i = 0; i < 15; i++) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount).insert(testContext.getJdbi());

            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withAmount(((long) 100 + i))
                    .withReference("MBK" + i)
                    .withDescription("Description" + i)
                    .withCreatedDate(createdDate)
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .withName("J. Doe" + i)
                    .insert(testContext.getJdbi());
        }

        String requestPath = "/v1/api/accounts/{accountId}/payments?page=:page&display_size=:display_size&from_date=:fromDate&to_date=:toDate"
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
                .body("results", hasSize(5))
                .body("total", is(15))
                .body("count", is(5))
                .body("page", is(2))
                .body("results[0].reference", is("MBK4"))
                .body("results[4].description", is("Description0"))
                .body("results[2].created_date", is(createdDate.format(ISO_INSTANT_MILLISECOND_PRECISION)));
    }

    @Test
    public void shouldReturn3Records_whenFromAndToDateSet() {
        ZonedDateTime fromDate = ZonedDateTime.now(ZoneOffset.UTC).minusYears(1).minusDays(4L).minusMinutes(5L);
        ZonedDateTime toDate = ZonedDateTime.now(ZoneOffset.UTC).minusYears(1).minusDays(2L).plusMinutes(5L);
        for (int i = 0, day = 15; i < 15; i++, day--) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount).insert(testContext.getJdbi());

            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withAmount((long) 100 + i)
                    .withReference("MBK" + day)
                    .withDescription("Description" + i)
                    .withCreatedDate(ZonedDateTime.now(ZoneOffset.UTC).minusYears(1).minusDays(day))
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .withName("J. Doe" + day)
                    .withEmail("j.doe@mail.fake")
                    .insert(testContext.getJdbi());
        }

        String requestPath = "/v1/api/accounts/{accountId}/payments?page=:page&display_size=:display_size&from_date=:fromDate&to_date=:toDate"
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
                .body("results", hasSize(3))
                .body("page", is(1))
                .body("total", is(3))
                .body("count", is(3))
                .body("results[0].reference", is("MBK2"))
                .body("results[2].description", is("Description11"));
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
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", contains("Input toDate (2018-14-08T15:00Z) is wrong format"))
                .body("error_identifier", is(ErrorIdentifier.GENERIC.toString()));
    }

    @Test
    public void shouldReturn5Records_whenReferenceSet_ignoreCase() {
        List<String> referenceList = Arrays.asList("REF1", "REF2");
        for (int i = 0; i < 15; i++) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount).insert(testContext.getJdbi());

            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference(i % 3 == 0 ? referenceList.get(0) : referenceList.get(1))
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .insert(testContext.getJdbi());
        }

        String requestPath = "/v1/api/accounts/{accountId}/payments?reference=:reference"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":reference", "f1");
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("results", hasSize(5));
    }

    @Test
    public void shouldReturn6Records_whenAmountSet() {
        List<Long> amountList = Arrays.asList(1051L, 2343L, 1111L);
        for (int i = 0; i < 20; i++) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount).insert(testContext.getJdbi());

            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withAmount(i % 3 == 0 ? amountList.get(0) : i % 2 == 0 ? amountList.get(1) : amountList.get(2))
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .insert(testContext.getJdbi());
        }

        String requestPath = "/v1/api/accounts/{accountId}/payments?amount=:amount"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace(":amount", "2343");
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("results", hasSize(6));
    }

    @Test
    public void shouldReturn3Records_whenSearchingByMandateId() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withExternalId("gateway-external-id")
                .insert(testContext.getJdbi());
        MandateExternalId mandateExternalId = MandateExternalId.valueOf("a-mandate-external-id");
        MandateExternalId anotherMandateExternalId = MandateExternalId.valueOf("another-external-id");
        MandateFixture mandateFixture1 = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(mandateExternalId)
                .insert(testContext.getJdbi());
        MandateFixture mandateFixture2 = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(anotherMandateExternalId)
                .insert(testContext.getJdbi());
        PayerFixture payerFixture1 = aPayerFixture()
                .withMandateId(mandateFixture1.getId())
                .withEmail("j.citizen@mail.test")
                .withName("J. Citizen")
                .insert(testContext.getJdbi());
        aPayerFixture()
                .withMandateId(mandateFixture2.getId())
                .withEmail("j.doe@mail.test")
                .withName("J. Doe")
                .insert(testContext.getJdbi());
        for (int i = 0; i < 6; i++) {
            if (i % 2 == 0) {
                aPaymentFixture()
                        .withMandateFixture(mandateFixture2)
                        .insert(testContext.getJdbi());
                continue;
            }
            aPaymentFixture()
                    .withMandateFixture(mandateFixture1)
                    .insert(testContext.getJdbi());
        }
        String requestPath = "/v1/api/accounts/{accountId}/payments?mandate_id=:mandateId"
                .replace("{accountId}", gatewayAccountFixture.getExternalId())
                .replace(":mandateId", mandateFixture1.getExternalId().toString());
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("count", is(3))
                .body("results", hasSize(3));
    }

    @Test
    public void shouldReturn6Records_whenSearchingByFailedExternalState() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withExternalId("gateway-external-id")
                .insert(testContext.getJdbi());
        MandateExternalId mandateExternalId = MandateExternalId.valueOf("a-mandate-external-id");
        MandateFixture mandateFixture = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(mandateExternalId)
                .insert(testContext.getJdbi());
        aPayerFixture()
                .withMandateId(mandateFixture.getId())
                .withEmail("j.citizen@mail.test")
                .withName("J. Citizen")
                .insert(testContext.getJdbi());
        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.CREATED)
                .insert(testContext.getJdbi());
        
        List.of(1,2,3).forEach(i -> {
            aPaymentFixture()
                    .withMandateFixture(mandateFixture)
                    .withState(PaymentState.CANCELLED)
                    .withStateDetails("state_details")
                    .insert(testContext.getJdbi());
        });
        
        String requestPath = "/v1/api/accounts/{accountId}/payments?state=:state"
                .replace("{accountId}", gatewayAccountFixture.getExternalId())
                .replace(":state", ExternalPaymentState.EXTERNAL_FAILED.getStatus());
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("count", is(3))
                .body("results", hasSize(3))
                .body("results[0].state.finished", is(true))
                .body("results[0].state.status", is("failed"))
                .body("results[0].state.details", is("state_details"));
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
