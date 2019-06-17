package uk.gov.pay.directdebit.mandate.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture.aGoCardlessCustomerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateCustomer;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateCustomerBankAccount;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateMandate;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubGetCreditor;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;
import static uk.gov.pay.directdebit.util.ResponseContainsLinkMatcher.containsLink;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class MandateResourceIT {

    private static final String JSON_AMOUNT_KEY = "amount";
    private static final String JSON_REFERENCE_KEY = "reference";
    private static final String JSON_DESCRIPTION_KEY = "description";
    private static final String JSON_STATE_KEY = "state.status";
    private static final String JSON_MANDATE_ID_KEY = "mandate_id";

    @DropwizardTestContext
    private TestContext testContext;
    //todo we should be able to override this in the test-it-config or else tests won't easily run in parallel. See https://payments-platform.atlassian.net/browse/PP-3374
    @Rule
    public WireMockRule wireMockRuleGoCardless = new WireMockRule(10107);

    private WireMockServer wireMockAdminUsers = new WireMockServer(options().port(10110));

    @After
    public void tearDown() {
        wireMockAdminUsers.shutdown();
    }

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();

    private PayerFixture payerFixture = PayerFixture.aPayerFixture();
    
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        wireMockAdminUsers.start();
        gatewayAccountFixture.insert(testContext.getJdbi());
    }
    
    @Test
    @Parameters({
            "null, test-service-ref, Field [return_url] cannot be null",
            " , test-service-ref, Field [return_url] must have a size between 1 and 255",
            //TODO Re-enable once pact tests are passing and validation is re-enabled.
            //"http://example, null, Field [service_reference] cannot be null",
            "http://example, , Field [service_reference] must have a size between 1 and 255"
    })
    public void createMandateValidationFailures(@Nullable String returnUrl, 
                                                @Nullable String serviceReference, 
                                                String expectedErrorMessage) throws Exception {
        String accountExternalId = gatewayAccountFixture.getExternalId();
        
        Map<String, String> createMandateRequest = new HashMap<>();
        Optional.ofNullable(returnUrl).ifPresent(x -> createMandateRequest.put("return_url", x));
        Optional.ofNullable(serviceReference).ifPresent(x -> createMandateRequest.put("service_reference", x));

        givenSetup()
                .body(objectMapper.writeValueAsString(createMandateRequest))
                .post("/v1/api/accounts/{accountId}/mandates".replace("{accountId}", accountExternalId))
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("message", contains(expectedErrorMessage))
                .body("error_identifier", is("GENERIC"));
    }

    @Test
    @Parameters({"null", "raindrops on roses and whiskers on kittens"})
    public void shouldCreateAMandateSuccessfully(@Nullable String description) throws Exception {
        String accountExternalId = gatewayAccountFixture.getExternalId();
        String returnUrl = "http://example.com/success-page/";
        
        ImmutableMap.Builder createMandateBuilder = ImmutableMap.builder()
                .put("return_url", returnUrl)
                .put("service_reference", "test-service-reference");
        
        if (description != null) {
            createMandateBuilder.put("description", description);
        }
        
        String requestPath = "/v1/api/accounts/{accountId}/mandates".replace("{accountId}", accountExternalId);

        ValidatableResponse response = givenSetup()
                .body(objectMapper.writeValueAsString(createMandateBuilder.build()))
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body(JSON_MANDATE_ID_KEY, is(notNullValue()))
                .body("return_url", is(returnUrl))
                .body("created_date", is(notNullValue()))
                .body("state.status", is("created"))
                .body("state.finished", is(false))
                .body("service_reference", is("test-service-reference"))
                .body("mandate_reference", is(notNullValue()))
                .body("description", optionalDescriptionMatcher(description))
                .contentType(JSON);
        MandateExternalId externalMandateId = MandateExternalId.valueOf(response.extract().path(JSON_MANDATE_ID_KEY).toString());

        String documentLocation = expectedMandateLocationFor(accountExternalId, externalMandateId);
        String token = testContext.getDatabaseTestHelper().getTokenByMandateExternalId(externalMandateId).get("secure_redirect_token").toString();

        String hrefNextUrl = "http://Frontend/secure/" + token;
        String hrefNextUrlPost = "http://Frontend/secure";

        response.body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", hrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", token);
                }}));
    }

    private Matcher<Object> optionalDescriptionMatcher(String description) {
        return description == null ? is(nullValue()) : is(notNullValue());
    }

    @Test
    public void shouldRetrieveAMandate_FromFrontendEndpoint_WhenAPaymentHasBeenCreated() {
        String accountExternalId = gatewayAccountFixture.getExternalId();
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withPayerFixture(payerFixture)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        PaymentFixture paymentFixture = createPaymentFixtureWith(mandateFixture, PaymentState.NEW);

        String requestPath = format("/v1/accounts/%s/mandates/%s/payments/%s",
                accountExternalId, 
                mandateFixture.getExternalId().toString(),
                paymentFixture.getExternalId());

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .log().body()
                .body("external_id", is(mandateFixture.getExternalId().toString()))
                .body("gateway_account_id", isNumber(gatewayAccountFixture.getId()))
                .body("gateway_account_external_id", is(gatewayAccountFixture.getExternalId()))
                .body("state.status", is(mandateFixture.getState().toExternal().getState()))
                .body("internal_state", is(mandateFixture.getState().toString()))
                .body("mandate_reference", is(mandateFixture.getMandateReference().toString()))
                .body("created_date", is(mandateFixture.getCreatedDate().format(ISO_INSTANT_MILLISECOND_PRECISION)))
                .body("payment." + JSON_AMOUNT_KEY, isNumber(paymentFixture.getAmount()))
                .body("payment." + JSON_REFERENCE_KEY, is(paymentFixture.getReference()))
                .body("payment." + JSON_DESCRIPTION_KEY, is(paymentFixture.getDescription()))
                .body("payment." + JSON_STATE_KEY, is(paymentFixture.getState().toExternal().getState()))
                .body("payer.payer_external_id", is(payerFixture.getExternalId()))
                .body("payer.account_holder_name", is(payerFixture.getName()))
                .body("payer.email", is(payerFixture.getEmail()))
                .body("payer.requires_authorisation", is(payerFixture.getAccountRequiresAuthorisation()));
    }

    @Test
    public void shouldRetrieveAMandate_FromFrontendEndpoint_WhenNoPaymentHasBeenCreated() {

        String accountExternalId = gatewayAccountFixture.getExternalId();
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withPayerFixture(payerFixture)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        String frontendMandatePath = "/v1/accounts/{accountId}/mandates/{mandateExternalId}";
        String requestPath = frontendMandatePath
                .replace("{accountId}", accountExternalId)
                .replace("{mandateExternalId}", mandateFixture.getExternalId().toString());

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body("external_id", is(mandateFixture.getExternalId().toString()))
                .body("gateway_account_id", isNumber(gatewayAccountFixture.getId()))
                .body("gateway_account_external_id", is(gatewayAccountFixture.getExternalId()))
                .body("state.status", is(mandateFixture.getState().toExternal().getState()))
                .body("internal_state", is(mandateFixture.getState().toString()))
                .body("mandate_reference", is(mandateFixture.getMandateReference().toString()))
                .body("created_date", is(mandateFixture.getCreatedDate().format(ISO_INSTANT_MILLISECOND_PRECISION)))
                .body("$", not(hasKey("payment")))
                .body("payer.payer_external_id", is(payerFixture.getExternalId()))
                .body("payer.account_holder_name", is(payerFixture.getName()))
                .body("payer.email", is(payerFixture.getEmail()))
                .body("payer.requires_authorisation", is(payerFixture.getAccountRequiresAuthorisation()));
    }

    @Test
    public void shouldRetrieveAMandate_FromPublicApiEndpoint() {
        String accountExternalId = gatewayAccountFixture.getExternalId();
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withPayerFixture(payerFixture)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        String publicApiMandatePath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}";
        String requestPath = publicApiMandatePath
                .replace("{accountId}", accountExternalId)
                .replace("{mandateExternalId}", mandateFixture.getExternalId().toString());

        ValidatableResponse getMandateResponse = givenSetup()
                .get(requestPath)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body("mandate_id", is(mandateFixture.getExternalId().toString()))
                .body("return_url", is(mandateFixture.getReturnUrl()))
                .body("state.status", is(mandateFixture.getState().toExternal().getState()))
                .body("state.finished", is(mandateFixture.getState().toExternal().isFinished()))
                .body("service_reference", is(mandateFixture.getServiceReference()))
                .body("mandate_reference", is(notNullValue()));

        String token = testContext.getDatabaseTestHelper().getTokenByMandateExternalId(mandateFixture.getExternalId()).get("secure_redirect_token").toString();
        String documentLocation = expectedMandateLocationFor(accountExternalId, mandateFixture.getExternalId());

        String hrefNextUrl = "http://Frontend/secure/" + token;
        String hrefNextUrlPost = "http://Frontend/secure";

        getMandateResponse.body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", hrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", token);
                }}));

    }

    @Test
    public void shouldCancelAMandate() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/cancel"
                .replace("{accountId}", gatewayAccountFixture.getExternalId())
                .replace("{mandateExternalId}", mandateFixture.getExternalId().toString());

        givenSetup()
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(payment.get("state"), is("CANCELLED"));
    }

    @Test
    public void shouldChangePaymentType() {
        MandateFixture testMandate = MandateFixture.aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/change-payment-method"
                .replace("{accountId}", gatewayAccountFixture.getExternalId())
                .replace("{mandateExternalId}", testMandate.getExternalId().toString());

        givenSetup()
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getMandateById(testMandate.getId());
        assertThat(payment.get("state"), is("USER_CANCEL_NOT_ELIGIBLE"));
    }

    @Test
    public void confirm_shouldCreateAMandateWithoutPayment_ifMandateIsOnDemand() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());

        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length() - 2);

        // language=JSON
        String emailPayloadBody = "{\n" +
                "  \"address\": \"" + payerFixture.getEmail() + "\",\n" +
                "  \"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\",\n" +
                "  \"template\": \"ON_DEMAND_MANDATE_CREATED\",\n" +
                "  \"personalisation\": {\n" +
                "    \"mandate reference\": \"" + mandateFixture.getMandateReference() + "\",\n" +
                "    \"bank account last 2 digits\": \"" + lastTwoDigitsBankAccount + "\",\n" +
                "    \"statement name\": \"Sandbox SUN Name\",\n" +
                "    \"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"\n" +
                "  }\n" +
                "}\n";
        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(aResponse().withStatus(200)));

        // language=JSON
        String confirmDetails = "{\n" +
                "  \"sort_code\": \"" + payerFixture.getSortCode() + "\",\n" +
                "  \"account_number\": \"" + payerFixture.getAccountNumber() + "\"\n" +
                "}\n";

        String requestPath = format("/v1/api/accounts/%s/mandates/%s/confirm", gatewayAccountFixture.getExternalId(), mandateFixture.getExternalId());
        given().port(testContext.getPort())
                .body(confirmDetails)
                .contentType(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        List<Map<String, Object>> paymentsForMandate = testContext.getDatabaseTestHelper().getPaymentsForMandate(mandateFixture.getExternalId());
        MatcherAssert.assertThat(paymentsForMandate, is(empty()));
    }

    @Test
    public void confirm_shouldCreateACustomerBankAccountMandateWithNoPayment_ForGoCardless_ifMandateIsOnDemand() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS).insert(testContext.getJdbi());
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());

        String customerId = "CU000358S3A2FP";
        String customerBankAccountId = "BA0002WR3Z193A";
        String sunName = "Test SUN Name";
        GoCardlessCustomerFixture goCardlessCustomerFixture = aGoCardlessCustomerFixture().
                withCustomerId(customerId)
                .withCustomerBankAccountId(customerBankAccountId)
                .withPayerId(payerFixture.getId());
        stubCreateCustomer(gatewayAccountFixture.getAccessToken().toString(), mandateFixture.getExternalId().toString(), payerFixture, customerId);
        stubCreateCustomerBankAccount(gatewayAccountFixture.getAccessToken().toString(), mandateFixture.getExternalId().toString(), payerFixture, customerId, customerBankAccountId);
        stubCreateMandate(gatewayAccountFixture.getAccessToken().toString(), mandateFixture.getExternalId().toString(), goCardlessCustomerFixture);
        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length() - 2);
        stubGetCreditor(gatewayAccountFixture.getAccessToken().toString(), sunName);

        String emailPayloadBody = "{\n" +
                "  \"address\": \"" + payerFixture.getEmail() + "\",\n" +
                "  \"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\",\n" +
                "  \"template\": \"ON_DEMAND_MANDATE_CREATED\",\n" +
                "  \"personalisation\": {\n" +
                "    \"mandate reference\": \"" + mandateFixture.getMandateReference() + "\",\n" +
                "    \"bank account last 2 digits\": \"" + lastTwoDigitsBankAccount + "\",\n" +
                "    \"statement name\": \"" + sunName + "\",\n" +
                "    \"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"\n" +
                "  }\n" +
                "}\n";
        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(aResponse().withStatus(200)));

        String confirmDetails = "{\n" +
                "  \"sort_code\": \"" + payerFixture.getSortCode() + "\",\n" +
                "  \"account_number\": \"" + payerFixture.getAccountNumber() + "\"\n" +
                "}\n";

        String requestPath = format("/v1/api/accounts/%s/mandates/%s/confirm",
                gatewayAccountFixture.getExternalId(), mandateFixture.getExternalId());
        given().port(testContext.getPort())
                .contentType(APPLICATION_JSON)
                .body(confirmDetails)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        List<Map<String, Object>> paymentsForMandate = testContext.getDatabaseTestHelper().getPaymentsForMandate(mandateFixture.getExternalId());
        MatcherAssert.assertThat(paymentsForMandate, is(empty()));
    }

    private PaymentFixture createPaymentFixtureWith(MandateFixture mandateFixture, PaymentState paymentState) {
        return aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(paymentState)
                .insert(testContext.getJdbi());
    }

    private String expectedMandateLocationFor(String accountId, MandateExternalId mandateExternalId) {
        return "http://localhost:" + testContext.getPort() + "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}"
                .replace("{accountId}", accountId)
                .replace("{mandateExternalId}", mandateExternalId.toString());
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }

}
