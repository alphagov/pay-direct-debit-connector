package uk.gov.pay.directdebit.mandate.resources;

import io.restassured.specification.RequestSpecification;
import junitparams.Parameters;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class MandateSearchResourceIT {

    @DropwizardTestContext
    private TestContext testContext;

    private static final PaymentProvider PAYMENT_PROVIDER = PaymentProvider.SANDBOX;
    private static final String DESCRIPTION = "is awesome";
    private static final String ANALYTICS_ID = "DD_234098_BBBLA";
    private static final GatewayAccount.Type TYPE = GatewayAccount.Type.TEST;
    private static final String EXTERNAL_ID = "osiuoisajd";

    private GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture();

    @Before
    public void setUp() {
        gatewayAccountFixture
                .withExternalId(EXTERNAL_ID)
                .withPaymentProvider(PAYMENT_PROVIDER)
                .withDescription(DESCRIPTION)
                .withType(TYPE)
                .withAnalyticsId(ANALYTICS_ID)
                .insert(testContext.getJdbi());
    }
    
    @Test
    @Parameters({
            "from_date, 2018-14-05T15:00Z, Invalid attribute value: from_date. Must be a valid date", 
            "to_date, 2018-14-05T15:00Z, Invalid attribute value: to_date. Must be a valid date", 
            "page, -1, Invalid attribute value: page. Must be greater than or equal to 1", 
            "display_size, 0, Invalid attribute value: display_size. Must be greater than or equal to 1", 
            "display_size, 501, Invalid attribute value: display_size. Must be less than or equal to 500"
    })
    public void searchWithInvalidParams(String param, String value, String expectedErrorMessage) {
        givenSetup()
                .queryParams(Map.of(param, value))
                .get(format("/v1/api/accounts/%s/mandates", gatewayAccountFixture.getExternalId()))
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("message", contains(expectedErrorMessage))
                .body("error_identifier", is(ErrorIdentifier.GENERIC.toString()));
    }
    
    @Test
    public void searchByNonExistentGatewayAccount() {
        givenSetup()
                .get("/v1/api/accounts/nexiste-pas/mandates")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }
    
    @Test
    public void searchSuccessfully() {
        PayerFixture payerFixture = aPayerFixture().withName("Joe Bloggs").withEmail("j.bloggs@example.org");
        MandateFixture mandateFixture = aMandateFixture()
                .withServiceReference("a service ref")
                .withState(MandateState.PENDING)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withMandateBankStatementReference(MandateBankStatementReference.valueOf("a bstatement ref"))
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());

        Map<String, Object> params = Map.of(
                "reference", "a service ref",
                "state", "pending",
                "bank_statement_reference", "a bstatement ref",
                "name", payerFixture.getName(),
                "email", payerFixture.getEmail(),
                "from_date", ZonedDateTime.now(ZoneOffset.UTC).minusDays(1).toString(),
                "to_date", ZonedDateTime.now(ZoneOffset.UTC).plusDays(1).toString(),
                "page", 0,
                "display_size", 10
        );

        givenSetup()
                .queryParams(params)
                .get(format("/v1/api/accounts/%s/mandates", gatewayAccountFixture.getExternalId()))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(JSON)
                .log().body()
                .body("results", hasSize(1))
                .body("results[0].mandate_id", is(mandateFixture.getExternalId().toString()))
                .body("results[0].return_url", isNumber(gatewayAccountFixture.getId()))
                .body("results[0].service_reference", is(gatewayAccountFixture.getExternalId()))
                .body("results[0].state.status", is(mandateFixture.getState().toExternal().getState()))
                .body("results[0].state.details", is("example details"))
                .body("results[0].state.finished", is(mandateFixture.getState().toString()))
                .body("results[0].mandate_reference", is(mandateFixture.getMandateReference().toString()))
                .body("results[0].created_date", is(mandateFixture.getCreatedDate().format(ISO_INSTANT_MILLISECOND_PRECISION)))
                .body("results[0].provider_id", is(payerFixture.getExternalId()))
                .body("results[0].payment_provider", is(payerFixture.getName()))
                .body("results[0].payer.email", is(payerFixture.getEmail()))
                .body("results[0].payer.name", is(payerFixture.getAccountRequiresAuthorisation()));

    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort()).contentType(JSON);
    }
}
