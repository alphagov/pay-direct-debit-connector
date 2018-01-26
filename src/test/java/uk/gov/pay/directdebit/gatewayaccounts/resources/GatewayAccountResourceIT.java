package uk.gov.pay.directdebit.gatewayaccounts.resources;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.gatewayaccounts.resources.GatewayAccountResource.GATEWAY_ACCOUNTS_API_PATH;
import static uk.gov.pay.directdebit.gatewayaccounts.resources.GatewayAccountResource.GATEWAY_ACCOUNT_API_PATH;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GatewayAccountResourceIT {

    private static final PaymentProvider PAYMENT_PROVIDER = PaymentProvider.SANDBOX;
    private static final String SERVICE_NAME = "alex";
    private static final String DESCRIPTION = "is awesome";
    private static final String ANALYTICS_ID = "DD_234098_BBBLA";
    private static final GatewayAccount.Type TYPE = GatewayAccount.Type.TEST;

    private static final String PAYMENT_PROVIDER_KEY = "payment_provider";
    private static final String TYPE_KEY = "type";
    private static final String SERVICE_NAME_KEY = "service_name";
    private static final String DESCRIPTION_KEY = "description";
    private static final String ANALYTICS_ID_KEY = "analytics_id";

    Gson gson = new Gson();

    @DropwizardTestContext
    private TestContext testContext;

    private GatewayAccountFixture testGatewayAccount;

    @Before
    public void setUp() {
        testGatewayAccount =
                aGatewayAccountFixture()
                        .withPaymentProvider(PAYMENT_PROVIDER)
                        .withServiceName(SERVICE_NAME)
                        .withDescription(DESCRIPTION)
                        .withType(TYPE)
                        .withAnalyticsId(ANALYTICS_ID)
                        .insert(testContext.getJdbi());
    }

    @Test
    public void shouldReturnAGatewayAccountIfItExists() {
        String requestPath = GATEWAY_ACCOUNT_API_PATH.replace("{accountId}", testGatewayAccount.getId().toString());
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body(PAYMENT_PROVIDER_KEY, is(PAYMENT_PROVIDER.toString()))
                .body(TYPE_KEY, is(TYPE.toString()))
                .body(SERVICE_NAME_KEY, is(SERVICE_NAME))
                .body(DESCRIPTION_KEY, is(DESCRIPTION))
                .body(ANALYTICS_ID_KEY, is(ANALYTICS_ID));
    }

    @Test
    public void shouldReturnAGatewayAccountWithMinimalFields() {
        GatewayAccountFixture testGatewayAccount2 = GatewayAccountFixture.aGatewayAccountFixture()
                .withServiceName("service")
                .withPaymentProvider(PaymentProvider.GOCARDLESS)
                .withType(GatewayAccount.Type.LIVE)
                .withDescription(null)
                .withAnalyticsId(null)
                .insert(testContext.getJdbi());

        String requestPath = GATEWAY_ACCOUNT_API_PATH.replace("{accountId}", testGatewayAccount2.getId().toString());
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body(PAYMENT_PROVIDER_KEY, is(PaymentProvider.GOCARDLESS.toString()))
                .body(TYPE_KEY, is(GatewayAccount.Type.LIVE.toString()))
                .body(SERVICE_NAME_KEY, is("service"))
                .body("containsKey('description')", is(false))
                .body("containsKey('analytics_id')", is(false));
    }

    private String expectedGatewayAccountLocationFor(Long accountId) {
        return "http://localhost:" + testContext.getPort() + GATEWAY_ACCOUNT_API_PATH
                .replace("{accountId}", accountId.toString());
    }
    @Test
    public void shouldReturnAllGatewayAccounts() {
        PaymentProvider paymentProvider2 = PaymentProvider.GOCARDLESS;
        String serviceName2 = "silvia";
        String description2 = "can't type and is not drunk maybe";
        String analyticsId2 = "DD_234098_BBBLABLA";

        GatewayAccountFixture testGatewayAccount2 = GatewayAccountFixture.aGatewayAccountFixture()
                .withServiceName(serviceName2)
                .withDescription(description2)
                .withPaymentProvider(paymentProvider2)
                .withAnalyticsId(analyticsId2)
                .insert(testContext.getJdbi());

        givenSetup()
                .get(GATEWAY_ACCOUNTS_API_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("accounts", hasSize(2))

                .body(format("accounts[0].%s", PAYMENT_PROVIDER_KEY), is(PAYMENT_PROVIDER.toString()))
                .body(format("accounts[0].%s", SERVICE_NAME_KEY), is(SERVICE_NAME))
                .body(format("accounts[0].%s", DESCRIPTION_KEY), is(DESCRIPTION))
                .body(format("accounts[0].%s", ANALYTICS_ID_KEY), is(ANALYTICS_ID))
                .body(format("accounts[0].%s", TYPE_KEY), is(TYPE.toString()))
                .body("accounts[0].links.self.href", is(expectedGatewayAccountLocationFor(testGatewayAccount.getId())))

                .body(format("accounts[1].%s", PAYMENT_PROVIDER_KEY), is(paymentProvider2.toString()))
                .body(format("accounts[1].%s", SERVICE_NAME_KEY), is(serviceName2))
                .body(format("accounts[1].%s", DESCRIPTION_KEY), is(description2))
                .body(format("accounts[1].%s", ANALYTICS_ID_KEY), is(analyticsId2))
                .body(format("accounts[1].%s", TYPE_KEY), is(TYPE.toString()))
                .body("accounts[1].links.self.href", is(expectedGatewayAccountLocationFor(testGatewayAccount2.getId())));

    }

    @Test
    public void shouldCreateAGatewayAccount() {
        String postBody = gson.toJson(ImmutableMap.builder()
                .put(PAYMENT_PROVIDER_KEY, PAYMENT_PROVIDER.toString())
                .put(TYPE_KEY, TYPE.toString())
                .put(SERVICE_NAME_KEY, SERVICE_NAME)
                .put(DESCRIPTION_KEY, DESCRIPTION)
                .put(ANALYTICS_ID_KEY, ANALYTICS_ID)
                .build());

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(GATEWAY_ACCOUNTS_API_PATH)
                .then()
                .contentType(JSON)
                .statusCode(Response.Status.CREATED.getStatusCode());

        Long id = response.extract().body().jsonPath().getLong("gateway_account_id");
        String documentLocation = expectedGatewayAccountLocationFor(id);

        response
                .header("Location", is(documentLocation))
                .body("service_name", is(SERVICE_NAME))
                .body("payment_provider", is(PAYMENT_PROVIDER.toString()))
                .body("type", is(TYPE.toString()))
                .body("description", is(DESCRIPTION))
                .body("analytics_id", is(ANALYTICS_ID));
    }

    @Test
    public void shouldReturnBadRequestIfValidationFails() {
        String postBody = gson.toJson(ImmutableMap.builder()
                .put(PAYMENT_PROVIDER_KEY, PAYMENT_PROVIDER.toString())
                .put(TYPE_KEY, TYPE.toString())
                .put(DESCRIPTION_KEY, DESCRIPTION)
                .put(ANALYTICS_ID_KEY, ANALYTICS_ID)
                .build());

        givenSetup()
                .body(postBody)
                .post(GATEWAY_ACCOUNTS_API_PATH)
                .then()
                .contentType(JSON)
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("message", is("Field(s) missing: [service_name]"));
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
