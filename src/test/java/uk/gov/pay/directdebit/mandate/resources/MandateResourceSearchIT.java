package uk.gov.pay.directdebit.mandate.resources;

import io.restassured.specification.RequestSpecification;
import junitparams.Parameters;
import org.apache.http.HttpStatus;
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
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.time.ZonedDateTime;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class MandateResourceSearchIT {

    @DropwizardTestContext
    private TestContext testContext;

    private GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture();

    @Before
    public void setUp() {
        gatewayAccountFixture.insert(testContext.getJdbi());
    }
    
    @Test
    @Parameters({
            "from_date, 2018-14-05T15:00Z, Invalid attribute value: from_date. Must be a valid date", 
            "to_date, 2018-14-05T15:00Z, Invalid attribute value: to_date. Must be a valid date", 
            "page, -1, Invalid attribute value: page. Must be greater than or equal to 1", 
            "display_size, 0, Invalid attribute value: display_size. Must be greater than or equal to 1", 
            "display_size, 501, Invalid attribute value: display_size. Must be less than or equal to 500",
            "state, INVALID, Invalid attribute value: state is not a valid mandate external state"
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
    public void shouldFindOneMandateUsingAllParams() {
        
        var matchingMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withId(100L)
                .withServiceReference("expectedReference")
                .withExternalId(MandateExternalId.valueOf("expectedExternalId"))
                .withMandateBankStatementReference(MandateBankStatementReference.valueOf("bankRef"))
                .withState(MandateState.PENDING)
                .withStateDetails("state_details")
                .withCreatedDate(ZonedDateTime.now().minusDays(2))
                .insert(testContext.getJdbi())
                .toEntity();

        var payerFixture = PayerFixture.aPayerFixture()
                .withMandateId(100L)
                .withEmail("expected@example.com")
                .withName("expectedName")
                .insert(testContext.getJdbi());

        var notMatchingMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withServiceReference("should not match")
                .insert(testContext.getJdbi());
        
        var params = Map.of(
                "reference", matchingMandate.getServiceReference(),
                "bank_statement_reference", matchingMandate.getMandateBankStatementReference().get().toString(),
                "state", matchingMandate.getState().toExternal().getState(),
                "name", payerFixture.getName(),
                "email", payerFixture.getEmail(),
                "page", "1",
                "display_size", "100",
                "to_date", ZonedDateTime.now().minusDays(2).toString(),
                "from_date", ZonedDateTime.now().minusDays(3).toString()
        );
        
        givenSetup()
                .queryParams(params)
                .get(format("/v1/api/accounts/%s/mandates", gatewayAccountFixture.getExternalId()))
                .then()
                .body("total", is(1))
                .body("count", is(1))
                .body("page", is(1))
                .body("results", hasSize(1))
                .body("results[0].service_reference", is("expectedReference"))
                .body("results[0].mandate_id", is("expectedExternalId"))
                .body("results[0].state.details", is("state_details"))
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void shouldFindTwoMandatesWithNoParams() {

        var matchingMandateOne = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withId(1L)
                .withServiceReference("matchingMandateOne")
                .insert(testContext.getJdbi());

        var matchingMandateTwo = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withId(2L)
                .withServiceReference("matchingMandateTwo")
                .insert(testContext.getJdbi());

        givenSetup()
                .get(format("/v1/api/accounts/%s/mandates", gatewayAccountFixture.getExternalId()))
                .then()
                .body("total", is(2))
                .body("count", is(2))
                .body("page", is(1))
                .body("results", hasSize(2))
                .body("results[0].service_reference", is("matchingMandateTwo"))
                .body("results[1].service_reference", is("matchingMandateOne"))
                .statusCode(HttpStatus.SC_OK);
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort()).contentType(JSON);
    }
}
