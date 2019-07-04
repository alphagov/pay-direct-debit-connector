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
            "display_size, 0, Query param 'display_size' should be between 1 and 500.", 
            "display_size, 501, Query param 'display_size' should be between 1 and 500."
    })
    public void searchByInvalidDisplaySize(String param, String value, String expectedErrorMessage) throws Exception {
        var uri = new URIBuilder(format("/v1/api/accounts/%s/mandates", gatewayAccountFixture.getExternalId()));
        uri.addParameter(param, value);

        givenSetup()
                .get(uri.build())
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", contains(expectedErrorMessage))
                .body("error_identifier", is(ErrorIdentifier.GENERIC.toString()));
    }
    
    @Test
    public void searchByMalformedDate() {
        
    }
    
    @Test
    public void searchByNonExistentGatewayAccount() {
        
    }
    
    @Test
    public void searchByInvalidPageNumber() {
        
    }

    @Test
    public void searchWithMissingGatewayAccountId() {
    }
    
    @Test
    public void searchSuccessfully() {
        
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort()).contentType(JSON);
    }
}
