package uk.gov.pay.directdebit.resources;

import org.junit.Rule;
import org.junit.Test;
import uk.gov.pay.directdebit.infra.PostgresResetRule;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.IntegrationTestsSuite.env;

public class HealthCheckResourceIT {

    @Rule
    public PostgresResetRule postgresResetRule = new PostgresResetRule(env());

    @Test
    public void healthcheck_shouldReturnHealthy() {

        given()
                .port(env().getPort())
                .contentType(JSON)
                .when()
                .accept(JSON)
                .get("/healthcheck")
                .then()
                .statusCode(OK.getStatusCode())
                .body("database.healthy", is(true))
                .body("ping.healthy", is(true));
    }
}
