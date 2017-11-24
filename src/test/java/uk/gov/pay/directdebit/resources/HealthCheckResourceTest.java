package uk.gov.pay.directdebit.resources;

import org.junit.Test;

import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.Is.is;

/**
 * This is test could be removed when persistence is configured
 */
public class HealthCheckResourceTest extends IntegrationTest {

    @Test
    public void healthcheck_shouldReturnHealthy() {

        givenSetup()
                .when()
                .accept(JSON)
                .get("/healthcheck")
                .then()
                .statusCode(OK.getStatusCode())
                .body("database.healthy", is(true))
                .body("ping.healthy", is(true));
    }
}
