package uk.gov.pay.directdebit.resources;

import io.restassured.specification.RequestSpecification;
import org.junit.ClassRule;
import uk.gov.pay.directdebit.infra.DropwizardAppWithPostgresRule;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

public class IntegrationTest {

    @ClassRule
    public static DropwizardAppWithPostgresRule app = new DropwizardAppWithPostgresRule();

    protected RequestSpecification givenSetup() {
        return given().port(app.getLocalPort())
                .contentType(JSON);
    }
}
