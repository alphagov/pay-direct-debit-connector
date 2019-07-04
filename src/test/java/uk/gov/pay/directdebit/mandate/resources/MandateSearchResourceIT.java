package uk.gov.pay.directdebit.mandate.resources;

import io.restassured.specification.RequestSpecification;
import junitparams.Parameters;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import javax.ws.rs.core.Response;

import java.nio.charset.Charset;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class MandateSearchResourceIT {

    @DropwizardTestContext
    private TestContext testContext;

    private GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture();

    @Before
    public void setUp() {
        gatewayAccountFixture.insert(testContext.getJdbi());
    }
    
    @Test
    @Parameters({
            "to_date, 2018-14-05T15:00Z, Input to_date (2018-14-05T15:00Z) is wrong format.", 
            "from_date, 2018-14-05T15:00Z, Input from_date (2018-14-05T15:00Z) is wrong format.", 
            "page, -1, Query param 'page' should be a non zero positive integer.", 
            "display_size, 0, Query param 'display_size' should be between 1 and 500.", 
            "display_size, 501, Query param 'display_size' should be between 1 and 500."
    })
    public void searchWithInvalidParams(String param, String value, String expectedErrorMessage) {
        givenSetup()
                .queryParams(Map.of(param, value))
                .get(format("/v1/api/accounts/%s/mandates", gatewayAccountFixture.getExternalId()))
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", contains(expectedErrorMessage))
                .body("error_identifier", is(ErrorIdentifier.GENERIC.toString()));
    }
    
    @Test
    public void searchByNonExistentGatewayAccount() {
        
    }
    
    @Test
    public void searchSuccessfully() {
        
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort()).contentType(JSON);
    }
}
