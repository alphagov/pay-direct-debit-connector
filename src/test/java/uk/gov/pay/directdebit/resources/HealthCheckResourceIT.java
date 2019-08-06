package uk.gov.pay.directdebit.resources;

import org.junit.Test;
import org.junit.Rule;
import uk.gov.pay.directdebit.junit.DropwizardAppWithPostgresRule;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.Is.is;

public class HealthCheckResourceIT {

    @Rule
    public DropwizardAppWithPostgresRule app = new DropwizardAppWithPostgresRule();

    @Test
    public void healthcheck_shouldReturnHealthy() {

        System.out.println("Running test");
        given()
                .port(app.getTestContext().getPort())
                .contentType(JSON)
                .when()
                .accept(JSON)
                .get("/healthcheck")
                .then()
                .statusCode(OK.getStatusCode())
                .body("postgresql.healthy", is(true))
                .body("ping.healthy", is(true));
    }
}
