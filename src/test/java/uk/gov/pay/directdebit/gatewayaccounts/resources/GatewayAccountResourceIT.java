package uk.gov.pay.directdebit.gatewayaccounts.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
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
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.gatewayaccounts.resources.GatewayAccountResource.GATEWAY_ACCOUNTS_API_PATH;
import static uk.gov.pay.directdebit.gatewayaccounts.resources.GatewayAccountResource.GATEWAY_ACCOUNTS_FRONTEND_PATH;
import static uk.gov.pay.directdebit.gatewayaccounts.resources.GatewayAccountResource.GATEWAY_ACCOUNT_API_PATH;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.util.ResponseContainsLinkMatcher.containsLink;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GatewayAccountResourceIT {

    private static final PaymentProvider PAYMENT_PROVIDER = PaymentProvider.SANDBOX;
    private static final String SERVICE_NAME = "alex";
    private static final String DESCRIPTION = "is awesome";
    private static final String ANALYTICS_ID = "DD_234098_BBBLA";
    private static final GatewayAccount.Type TYPE = GatewayAccount.Type.TEST;
    private static final String EXTERNAL_ID = "osiuoisajd";

    private static final String PAYMENT_PROVIDER_KEY = "payment_provider";
    private static final String EXTERNAL_ID_KEY = "gateway_account_external_id";
    private static final String TYPE_KEY = "type";
    private static final String SERVICE_NAME_KEY = "service_name";
    private static final String DESCRIPTION_KEY = "description";
    private static final String ANALYTICS_ID_KEY = "analytics_id";

    @DropwizardTestContext
    private TestContext testContext;

    private GatewayAccountFixture testGatewayAccount;

    @Before
    public void setUp() {
        testGatewayAccount =
                aGatewayAccountFixture()
                        .withExternalId(EXTERNAL_ID)
                        .withPaymentProvider(PAYMENT_PROVIDER)
                        .withServiceName(SERVICE_NAME)
                        .withDescription(DESCRIPTION)
                        .withType(TYPE)
                        .withAnalyticsId(ANALYTICS_ID)
                        .insert(testContext.getJdbi());
    }

    @Test
    public void shouldReturnAGatewayAccountIfItExists() {
        String requestPath = GATEWAY_ACCOUNT_API_PATH.replace("{accountId}", testGatewayAccount.getExternalId());
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body(PAYMENT_PROVIDER_KEY, is(PAYMENT_PROVIDER.toString()))
                .body(TYPE_KEY, is(TYPE.toString()))
                .body(EXTERNAL_ID_KEY, is(EXTERNAL_ID))
                .body(SERVICE_NAME_KEY, is(SERVICE_NAME))
                .body(DESCRIPTION_KEY, is(DESCRIPTION))
                .body(ANALYTICS_ID_KEY, is(ANALYTICS_ID));
    }

    @Test
    public void shouldReturnAGatewayAccountWithMinimalFields() {
        GatewayAccountFixture testGatewayAccount2 = GatewayAccountFixture.aGatewayAccountFixture()
                .withServiceName("service")
                .withExternalId("externalId")
                .withPaymentProvider(PaymentProvider.GOCARDLESS)
                .withType(GatewayAccount.Type.LIVE)
                .withDescription(null)
                .withAnalyticsId(null)
                .insert(testContext.getJdbi());

        String requestPath = GATEWAY_ACCOUNT_API_PATH.replace("{accountId}", testGatewayAccount2.getExternalId());
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body(PAYMENT_PROVIDER_KEY, is(PaymentProvider.GOCARDLESS.toString()))
                .body(TYPE_KEY, is(GatewayAccount.Type.LIVE.toString()))
                .body(SERVICE_NAME_KEY, is("service"))
                .body(EXTERNAL_ID_KEY, is("externalId"))
                .body("payment_method", is("DIRECT_DEBIT"))
                .body("containsKey('description')", is(false))
                .body("containsKey('analytics_id')", is(false));
    }

    private String expectedGatewayAccountLocationFor(String accountId) {
        return "http://localhost:" + testContext.getPort() + GATEWAY_ACCOUNT_API_PATH
                .replace("{accountId}", accountId);
    }

    @Test
    public void shouldReturnGatewayAccounts() {
        PaymentProvider paymentProvider2 = PaymentProvider.GOCARDLESS;
        String serviceName2 = "silvia";
        String description2 = "can't type and is not drunk maybe";
        String analyticsId2 = "DD_234098_BBBLABLA";
        String externalId2 = "DD_234098_BBBLABLA";

        GatewayAccountFixture testGatewayAccount2 = GatewayAccountFixture.aGatewayAccountFixture()
                .withExternalId(externalId2)
                .withServiceName(serviceName2)
                .withDescription(description2)
                .withPaymentProvider(paymentProvider2)
                .withAnalyticsId(analyticsId2)
                .insert(testContext.getJdbi());

        Arrays
          .asList(GATEWAY_ACCOUNTS_API_PATH, GATEWAY_ACCOUNTS_FRONTEND_PATH)
          .forEach(path -> {
            givenSetup()
              .get(path)
              .then()
              .statusCode(Response.Status.OK.getStatusCode())
              .contentType(JSON)
              .body("accounts", hasSize(2))

              .body(format("accounts[0].%s", PAYMENT_PROVIDER_KEY), is(PAYMENT_PROVIDER.toString()))
              .body(format("accounts[0].%s", SERVICE_NAME_KEY), is(SERVICE_NAME))
              .body(format("accounts[0].%s", DESCRIPTION_KEY), is(DESCRIPTION))
              .body(format("accounts[0].%s", ANALYTICS_ID_KEY), is(ANALYTICS_ID))
              .body(format("accounts[0].%s", EXTERNAL_ID_KEY), is(EXTERNAL_ID))
              .body(format("accounts[0].%s", TYPE_KEY), is(TYPE.toString()))
              .body("accounts[0].links", containsLink("self",
                    "GET",
                    expectedGatewayAccountLocationFor(testGatewayAccount.getExternalId())))

              .body(format("accounts[1].%s", PAYMENT_PROVIDER_KEY), is(paymentProvider2.toString()))
              .body(format("accounts[1].%s", SERVICE_NAME_KEY), is(serviceName2))
              .body(format("accounts[1].%s", DESCRIPTION_KEY), is(description2))
              .body(format("accounts[1].%s", ANALYTICS_ID_KEY), is(analyticsId2))
              .body(format("accounts[1].%s", EXTERNAL_ID_KEY), is(externalId2))
              .body(format("accounts[1].%s", TYPE_KEY), is(TYPE.toString()))
              .body("accounts[1].links", containsLink("self",
                    "GET",
                    expectedGatewayAccountLocationFor(testGatewayAccount2.getExternalId())));

            givenSetup()
              .queryParam("externalAccountIds", externalId2)
              .get(path)
              .then()
              .statusCode(Response.Status.OK.getStatusCode())
              .contentType(JSON)
              .body("accounts", hasSize(1))

              .body(format("accounts[0].%s", PAYMENT_PROVIDER_KEY), is(paymentProvider2.toString()))
              .body(format("accounts[0].%s", SERVICE_NAME_KEY), is(serviceName2))
              .body(format("accounts[0].%s", DESCRIPTION_KEY), is(description2))
              .body(format("accounts[0].%s", ANALYTICS_ID_KEY), is(analyticsId2))
              .body(format("accounts[0].%s", EXTERNAL_ID_KEY), is(externalId2))
              .body(format("accounts[0].%s", TYPE_KEY), is(TYPE.toString()))
              .body("accounts[0].links", containsLink("self",
                    "GET",
                    expectedGatewayAccountLocationFor(testGatewayAccount2.getExternalId())));
          });

    }

    @Test
    public void shouldReturnSomeGatewayAccounts() {
        PaymentProvider paymentProvider3 = PaymentProvider.GOCARDLESS;
        String serviceName3 = "toby";
        String description3 = "can't type and is not hungover maybe";
        String analyticsId3 = "DD_234099_BBBLABLA";
        String externalId3 = "DD_234099_BBBLABLA";

        GatewayAccountFixture testGatewayAccount3 = GatewayAccountFixture.aGatewayAccountFixture()
                .withExternalId(externalId3)
                .withServiceName(serviceName3)
                .withDescription(description3)
                .withPaymentProvider(paymentProvider3)
                .withAnalyticsId(analyticsId3)
                .insert(testContext.getJdbi());

        Arrays
          .asList(GATEWAY_ACCOUNTS_API_PATH, GATEWAY_ACCOUNTS_FRONTEND_PATH)
          .forEach(path -> {
            givenSetup()
              .queryParam(
                  "externalAccountIds",
                  String.format("%s,%s", EXTERNAL_ID, externalId3)
                  )
              .get(path)
              .then()
              .statusCode(Response.Status.OK.getStatusCode())
              .contentType(JSON)
              .body("accounts", hasSize(2))

              .body(format("accounts[0].%s", PAYMENT_PROVIDER_KEY), is(PAYMENT_PROVIDER.toString()))
              .body(format("accounts[0].%s", SERVICE_NAME_KEY), is(SERVICE_NAME))
              .body(format("accounts[0].%s", DESCRIPTION_KEY), is(DESCRIPTION))
              .body(format("accounts[0].%s", ANALYTICS_ID_KEY), is(ANALYTICS_ID))
              .body(format("accounts[0].%s", EXTERNAL_ID_KEY), is(EXTERNAL_ID))
              .body(format("accounts[0].%s", TYPE_KEY), is(TYPE.toString()))
              .body("accounts[0].links", containsLink("self",
                    "GET",
                    expectedGatewayAccountLocationFor(testGatewayAccount.getExternalId())))

              .body(format("accounts[1].%s", PAYMENT_PROVIDER_KEY), is(paymentProvider3.toString()))
              .body(format("accounts[1].%s", SERVICE_NAME_KEY), is(serviceName3))
              .body(format("accounts[1].%s", DESCRIPTION_KEY), is(description3))
              .body(format("accounts[1].%s", ANALYTICS_ID_KEY), is(analyticsId3))
              .body(format("accounts[1].%s", EXTERNAL_ID_KEY), is(externalId3))
              .body(format("accounts[1].%s", TYPE_KEY), is(TYPE.toString()))
              .body("accounts[1].links", containsLink("self",
                    "GET",
                    expectedGatewayAccountLocationFor(testGatewayAccount3.getExternalId())));
          });
    }

    @Test
    public void shouldCreateAGatewayAccount() throws JsonProcessingException {
        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
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

        String externalId = response.extract().body().jsonPath().getString("gateway_account_external_id");
        String documentLocation = expectedGatewayAccountLocationFor(externalId);

        response
                .header("Location", is(documentLocation))
                .body("gateway_account_id", is(notNullValue()))
                .body("gateway_account_external_id", startsWith("DIRECT_DEBIT:"))
                .body("service_name", is(SERVICE_NAME))
                .body("payment_provider", is(PAYMENT_PROVIDER.toString()))
                .body("type", is(TYPE.toString()))
                .body("description", is(DESCRIPTION))
                .body("analytics_id", is(ANALYTICS_ID));
    }

    @Test
    public void shouldReturnBadRequestIfValidationFails() throws JsonProcessingException {
        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
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
    
    @Test
    public void shouldUpdateAGatewayAccount_withAccessTokenAndOrganisation() {
        PaymentProvider paymentProvider3 = PaymentProvider.GOCARDLESS;
        String externalId4 = "DD_234099_BBBLABLA";
        String serviceName4 = "wilhelmina";
        String description4 = "can't type and is not hungover maybe";
        String analyticsId4 = "DD_234099_BBBLABLA";
        
        GatewayAccountFixture testGatewayAccount4 = GatewayAccountFixture.aGatewayAccountFixture()
                .withExternalId(externalId4)
                .withServiceName(serviceName4)
                .withDescription(description4)
                .withPaymentProvider(paymentProvider3)
                .withAnalyticsId(analyticsId4)
                .withAccessToken(null)
                .withOrganisation(null)
                .insert(testContext.getJdbi());
        ImmutableMap<String, String> accessTokenLoad = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "access_token")
                .put("value", "abcde1234")
                .build();
        ImmutableMap<String, String> organisationLoad = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "organisation")
                .put("value", "1234abcde")
                .build();
        JsonNode payload = new ObjectMapper().valueToTree(Arrays.asList(accessTokenLoad, organisationLoad));

        String requestPath = GATEWAY_ACCOUNT_API_PATH.replace("{accountId}", testGatewayAccount4.getExternalId());
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .body(payload)
                .patch(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body(PAYMENT_PROVIDER_KEY, is(PaymentProvider.GOCARDLESS.toString()))
                .body(TYPE_KEY, is(GatewayAccount.Type.TEST.toString()))
                .body(SERVICE_NAME_KEY, is(serviceName4))
                .body(EXTERNAL_ID_KEY, is(externalId4))
                .body("payment_method", is("DIRECT_DEBIT"))
                .body(DESCRIPTION_KEY, is(description4))
                .body(ANALYTICS_ID_KEY, is(analyticsId4))
                .body("containsKey('access_token')", is(false))
                .body("containsKey('organisation')", is(false));
        DatabaseTestHelper databaseTestHelper = new DatabaseTestHelper(testContext.getJdbi());
        Map<String, Object> foundGatewayAccount = databaseTestHelper.getGatewayAccountById(testGatewayAccount4.getId());
        assertThat(foundGatewayAccount.get("access_token"), Matchers.is("abcde1234"));
        assertThat(foundGatewayAccount.get("organisation"), Matchers.is("1234abcde"));
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
