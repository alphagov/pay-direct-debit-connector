package uk.gov.pay.directdebit.gatewayaccounts.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
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
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
    private static final String DESCRIPTION = "is awesome";
    private static final String ANALYTICS_ID = "DD_234098_BBBLA";
    private static final GatewayAccount.Type TYPE = GatewayAccount.Type.TEST;
    private static final String EXTERNAL_ID = "osiuoisajd";

    private static final String PAYMENT_PROVIDER_KEY = "payment_provider";
    private static final String EXTERNAL_ID_KEY = "gateway_account_external_id";
    private static final String TYPE_KEY = "type";
    private static final String DESCRIPTION_KEY = "description";
    private static final String ANALYTICS_ID_KEY = "analytics_id";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String ORGANISATION_KEY = "organisation";
    private static final String IS_CONNECTED = "is_connected";
    
    private static ObjectMapper objectMapper = new ObjectMapper();

    @DropwizardTestContext
    private TestContext testContext;

    private GatewayAccountFixture testGatewayAccount;

    @Before
    public void setUp() {
        testGatewayAccount = aGatewayAccountFixture()
                        .withExternalId(EXTERNAL_ID)
                        .withPaymentProvider(PAYMENT_PROVIDER)
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
                .body(DESCRIPTION_KEY, is(DESCRIPTION))
                .body(IS_CONNECTED, is(true))
                .body(ANALYTICS_ID_KEY, is(ANALYTICS_ID));
    }
    
    @Test
    public void isLinkedShouldBeFalseIfGatewayAccountHasNoAccessToken() {
        String externalId = aGatewayAccountFixture()
                .withExternalId(randomAlphabetic(25))
                .withPaymentProvider(PAYMENT_PROVIDER)
                .withDescription(DESCRIPTION)
                .withType(TYPE)
                .withAnalyticsId(ANALYTICS_ID)
                .withAccessToken(null)
                .insert(testContext.getJdbi())
                .getExternalId();

        givenSetup()
                .get(GATEWAY_ACCOUNT_API_PATH.replace("{accountId}", externalId))
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body(PAYMENT_PROVIDER_KEY, is(PAYMENT_PROVIDER.toString()))
                .body(TYPE_KEY, is(TYPE.toString()))
                .body(EXTERNAL_ID_KEY, is(externalId))
                .body(DESCRIPTION_KEY, is(DESCRIPTION))
                .body(IS_CONNECTED, is(false))
                .body(ANALYTICS_ID_KEY, is(ANALYTICS_ID));
    }

    @Test
    public void shouldReturnAGatewayAccountWithMinimalFields() {
        GatewayAccountFixture testGatewayAccount2 = GatewayAccountFixture.aGatewayAccountFixture()
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
                .body(EXTERNAL_ID_KEY, is("externalId"))
                .body("payment_method", is("DIRECT_DEBIT"))
                .body("containsKey('description')", is(false))
                .body("containsKey('analytics_id')", is(false));
    }

    private String expectedGatewayAccountLocationFor(String accountId) {
        return "http://localhost:" + testContext.getPort() + GATEWAY_ACCOUNT_API_PATH.replace("{accountId}", accountId);
    }

    @Test
    public void shouldReturnGatewayAccounts() {
        PaymentProvider paymentProvider2 = PaymentProvider.GOCARDLESS;
        String description2 = "can't type and is not drunk maybe";
        String analyticsId2 = "DD_234098_BBBLABLA";
        String externalId2 = "DD_234098_BBBLABLA";

        GatewayAccountFixture testGatewayAccount2 = GatewayAccountFixture.aGatewayAccountFixture()
                .withExternalId(externalId2)
                .withDescription(description2)
                .withPaymentProvider(paymentProvider2)
                .withAnalyticsId(analyticsId2)
                .insert(testContext.getJdbi());

        Arrays.asList(GATEWAY_ACCOUNTS_API_PATH, GATEWAY_ACCOUNTS_FRONTEND_PATH)
          .forEach(path -> {
            givenSetup()
              .get(path)
              .then()
              .statusCode(Response.Status.OK.getStatusCode())
              .contentType(JSON)
              .body("accounts", hasSize(2))

              .body(format("accounts[0].%s", PAYMENT_PROVIDER_KEY), is(PAYMENT_PROVIDER.toString()))
              .body(format("accounts[0].%s", DESCRIPTION_KEY), is(DESCRIPTION))
              .body(format("accounts[0].%s", ANALYTICS_ID_KEY), is(ANALYTICS_ID))
              .body(format("accounts[0].%s", EXTERNAL_ID_KEY), is(EXTERNAL_ID))
              .body(format("accounts[0].%s", TYPE_KEY), is(TYPE.toString()))
              .body("accounts[0].links", containsLink("self",
                    "GET",
                    expectedGatewayAccountLocationFor(testGatewayAccount.getExternalId())))

              .body(format("accounts[1].%s", PAYMENT_PROVIDER_KEY), is(paymentProvider2.toString()))
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
        String description3 = "can't type and is not hungover maybe";
        String analyticsId3 = "DD_234099_BBBLABLA";
        String externalId3 = "DD_234099_BBBLABLA";

        GatewayAccountFixture testGatewayAccount3 = GatewayAccountFixture.aGatewayAccountFixture()
                .withExternalId(externalId3)
                .withDescription(description3)
                .withPaymentProvider(paymentProvider3)
                .withAnalyticsId(analyticsId3)
                .insert(testContext.getJdbi());

        Arrays.asList(GATEWAY_ACCOUNTS_API_PATH, GATEWAY_ACCOUNTS_FRONTEND_PATH)
          .forEach(path -> givenSetup()
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
            .body(format("accounts[0].%s", DESCRIPTION_KEY), is(DESCRIPTION))
            .body(format("accounts[0].%s", ANALYTICS_ID_KEY), is(ANALYTICS_ID))
            .body(format("accounts[0].%s", EXTERNAL_ID_KEY), is(EXTERNAL_ID))
            .body(format("accounts[0].%s", TYPE_KEY), is(TYPE.toString()))
            .body("accounts[0].links", containsLink("self",
                  "GET",
                  expectedGatewayAccountLocationFor(testGatewayAccount.getExternalId())))

            .body(format("accounts[1].%s", PAYMENT_PROVIDER_KEY), is(paymentProvider3.toString()))
            .body(format("accounts[1].%s", DESCRIPTION_KEY), is(description3))
            .body(format("accounts[1].%s", ANALYTICS_ID_KEY), is(analyticsId3))
            .body(format("accounts[1].%s", EXTERNAL_ID_KEY), is(externalId3))
            .body(format("accounts[1].%s", TYPE_KEY), is(TYPE.toString()))
            .body("accounts[1].links", containsLink("self",
                  "GET",
                  expectedGatewayAccountLocationFor(testGatewayAccount3.getExternalId()))));
    }

    @Test
    public void shouldCreateAGatewayAccount() throws JsonProcessingException {
        String postBody = objectMapper.writeValueAsString(Map.of(
                PAYMENT_PROVIDER_KEY, PAYMENT_PROVIDER.toString(),
                TYPE_KEY, TYPE.toString(),
                DESCRIPTION_KEY, DESCRIPTION,
                ANALYTICS_ID_KEY, ANALYTICS_ID,
                ACCESS_TOKEN_KEY, "123",
                ORGANISATION_KEY, "anOrganisation"));

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(GATEWAY_ACCOUNTS_API_PATH)
                .then()
                .contentType(JSON)
                .statusCode(Response.Status.CREATED.getStatusCode());

        String externalId = response.extract().body().jsonPath().getString("gateway_account_external_id");
        String documentLocation = expectedGatewayAccountLocationFor(externalId);

        response.header("Location", is(documentLocation))
                .body("gateway_account_id", is(notNullValue()))
                .body("gateway_account_external_id", startsWith("DIRECT_DEBIT:"))
                .body("payment_provider", is(PAYMENT_PROVIDER.toString()))
                .body("type", is(TYPE.toString()))
                .body("description", is(DESCRIPTION))
                .body("is_connected", is(true))
                .body("analytics_id", is(ANALYTICS_ID));

        Long gatewayAccountId = response.extract().body().jsonPath().getLong("gateway_account_id");
        Map<String, Object> gatewayAccountMap = testContext.getDatabaseTestHelper().getGatewayAccountById(gatewayAccountId);
        assertThat(gatewayAccountMap.get("access_token"), is("123"));
        assertThat(gatewayAccountMap.get("organisation"), is("anOrganisation"));
    }
    
    @Test
    public void createGatewayAccountWithoutAccessTokenKey() throws Exception {
        String postBody = objectMapper.writeValueAsString(Map.of(
                PAYMENT_PROVIDER_KEY, PAYMENT_PROVIDER.toString(),
                TYPE_KEY, TYPE.toString(),
                DESCRIPTION_KEY, DESCRIPTION,
                ANALYTICS_ID_KEY, ANALYTICS_ID,
                ORGANISATION_KEY, "anOrganisation"));

        givenSetup()
                .body(postBody)
                .post(GATEWAY_ACCOUNTS_API_PATH)
                .then()
                .contentType(JSON)
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("is_connected", is(false));
    }

    @Test
    public void shouldCreateAGatewayAccountWithMinimumDetails() throws JsonProcessingException {
        String postBody = objectMapper.writeValueAsString(Map.of(
                PAYMENT_PROVIDER_KEY, PAYMENT_PROVIDER.toString(),
                TYPE_KEY, TYPE.toString()));

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(GATEWAY_ACCOUNTS_API_PATH)
                .then()
                .contentType(JSON)
                .statusCode(Response.Status.CREATED.getStatusCode());

        String externalId = response.extract().body().jsonPath().getString("gateway_account_external_id");
        String documentLocation = expectedGatewayAccountLocationFor(externalId);

        response.header("Location", is(documentLocation))
                .body("gateway_account_id", is(notNullValue()))
                .body("gateway_account_external_id", startsWith("DIRECT_DEBIT:"))
                .body("payment_provider", is(PAYMENT_PROVIDER.toString()))
                .body("type", is(TYPE.toString()))
                .body("description", is(nullValue()))
                .body("analytics_id", is(nullValue()));
    }

    @Test
    public void shouldReturnBadRequestIfValidationFails() throws JsonProcessingException {
        String postBody = objectMapper.writeValueAsString(Map.of(
                PAYMENT_PROVIDER_KEY, PAYMENT_PROVIDER.toString(),
                TYPE_KEY, TYPE.toString(),
                DESCRIPTION_KEY, "verylongdescription".repeat(25),
                ANALYTICS_ID_KEY, ANALYTICS_ID));

        givenSetup()
                .body(postBody)
                .post(GATEWAY_ACCOUNTS_API_PATH)
                .then()
                .contentType(JSON)
                .statusCode(422)
                .body("errors[0]", is("description size must be between 0 and 255"));
    }
    
    @Test
    public void shouldReturnBadRequestIfPaymentProviderIsInvalid() throws JsonProcessingException {
        String postBody = objectMapper.writeValueAsString(Map.of(
                PAYMENT_PROVIDER_KEY, "INVALID",
                TYPE_KEY, TYPE.toString(),
                DESCRIPTION_KEY, DESCRIPTION,
                ANALYTICS_ID_KEY, ANALYTICS_ID));

        givenSetup()
                .body(postBody)
                .post(GATEWAY_ACCOUNTS_API_PATH)
                .then()
                .contentType(JSON)
                .statusCode(400)
                .body("message", contains(containsString("Unsupported payment provider: INVALID")))
                .body("error_identifier", is(ErrorIdentifier.GENERIC.toString()));
    }

    @Test
    public void shouldUpdateAGatewayAccount_withAccessTokenAndOrganisation() {
        PaymentProvider paymentProvider3 = PaymentProvider.GOCARDLESS;
        String externalId4 = "DD_234099_BBBLABLA";
        String description4 = "can't type and is not hungover maybe";
        String analyticsId4 = "DD_234099_BBBLABLA";

        GatewayAccountFixture testGatewayAccount4 = GatewayAccountFixture.aGatewayAccountFixture()
                .withExternalId(externalId4)
                .withDescription(description4)
                .withPaymentProvider(paymentProvider3)
                .withAnalyticsId(analyticsId4)
                .withAccessToken(null)
                .withOrganisation(null)
                .insert(testContext.getJdbi());
        Map<String, String> accessTokenLoad = Map.of("op", "replace",
                "path", "access_token",
                "value", "abcde1234");
        Map<String, String> organisationLoad = Map.of("op", "replace",
                "path", "organisation",
                "value", "1234abcde");
        JsonNode payload = objectMapper.valueToTree(Arrays.asList(accessTokenLoad, organisationLoad));

        String requestPath = GATEWAY_ACCOUNT_API_PATH.replace("{accountId}", testGatewayAccount4.getExternalId());
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .body(payload)
                .patch(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
        DatabaseTestHelper databaseTestHelper = new DatabaseTestHelper(testContext.getJdbi());
        Map<String, Object> foundGatewayAccount = databaseTestHelper.getGatewayAccountById(testGatewayAccount4.getId());
        assertThat(foundGatewayAccount.get("access_token"), Matchers.is("abcde1234"));
        assertThat(foundGatewayAccount.get("organisation"), Matchers.is("1234abcde"));
    }

    @Test
    public void shouldFailWithBadRequest_whenNoPayload() {
        String requestPath = GATEWAY_ACCOUNT_API_PATH.replace("{accountId}", "an-external_id");
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .body("")
                .patch(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort()).contentType(JSON);
    }
}
