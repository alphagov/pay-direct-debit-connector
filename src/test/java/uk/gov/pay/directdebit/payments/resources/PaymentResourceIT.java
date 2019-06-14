package uk.gov.pay.directdebit.payments.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;
import static uk.gov.pay.directdebit.payments.resources.PaymentResource.CHARGE_API_PATH;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreatePayment;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubGetCreditor;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;
import static uk.gov.pay.directdebit.util.ResponseContainsLinkMatcher.containsLink;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentResourceIT {

    private static final String FRONTEND_CARD_DETAILS_URL = "/secure";
    private static final String JSON_AMOUNT_KEY = "amount";
    private static final String JSON_REFERENCE_KEY = "reference";
    private static final String JSON_DESCRIPTION_KEY = "description";
    private static final String JSON_GATEWAY_ACC_KEY = "gateway_account_id";
    private static final String JSON_RETURN_URL_KEY = "return_url";
    private static final String JSON_AGREEMENT_ID_KEY = "agreement_id";
    private static final String JSON_CHARGE_KEY = "charge_id";
    private static final String JSON_STATE_KEY = "state.status";
    private static final long AMOUNT = 6234L;
    private GatewayAccountFixture testGatewayAccount;

    @DropwizardTestContext
    private TestContext testContext;

    @Rule
    public WireMockRule wireMockRuleGoCardless = new WireMockRule(10107);

    private WireMockServer wireMockAdminUsers = new WireMockServer(options().port(10110));

    @After
    public void tearDown() {
        wireMockAdminUsers.shutdown();
    }

    @Before
    public void setUp() {
        wireMockAdminUsers.start();
        testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
    }

    @Test
    public void shouldCollectAPayment_forSandbox() throws Exception {
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());
        String accountExternalId = testGatewayAccount.getExternalId();
        String expectedReference = "Test reference";
        String expectedDescription = "Test description";
        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, AMOUNT)
                .put(JSON_REFERENCE_KEY, expectedReference)
                .put(JSON_DESCRIPTION_KEY, expectedDescription)
                .put(JSON_GATEWAY_ACC_KEY, accountExternalId)
                .put(JSON_AGREEMENT_ID_KEY, mandateFixture.getExternalId().toString())
                .build());

        String requestPath = "/v1/api/accounts/{accountId}/charges/collect"
                .replace("{accountId}", accountExternalId);
        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length() - 2);
        String chargeDate = LocalDate.now().plusDays(4).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        // language=JSON
        String emailPayloadBody = "{\n" +
                "  \"address\": \"" + payerFixture.getEmail() + "\",\n" +
                "  \"gateway_account_external_id\": \"" + testGatewayAccount.getExternalId() + "\",\n" +
                "  \"template\": \"ON_DEMAND_PAYMENT_CONFIRMED\",\n" +
                "  \"personalisation\": {\n" +
                "    \"amount\": \"" + BigDecimal.valueOf(AMOUNT, 2).toString() + "\",\n" +
                "    \"mandate reference\": \"" + mandateFixture.getMandateReference() + "\",\n" +
                "    \"bank account last 2 digits\": \"" + lastTwoDigitsBankAccount + "\",\n" +
                "    \"collection date\": \"" + chargeDate + "\",\n" +
                "    \"statement name\": \"Sandbox SUN Name\",\n" +
                "    \"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"\n" +
                "  }\n" +
                "}";

        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200)));

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body(JSON_CHARGE_KEY, is(notNullValue()))
                .body(JSON_AMOUNT_KEY, isNumber(AMOUNT))
                .body(JSON_REFERENCE_KEY, is(expectedReference))
                .body(JSON_DESCRIPTION_KEY, is(expectedDescription))
                .contentType(JSON);

        String externalTransactionId = response.extract().path(JSON_CHARGE_KEY).toString();

        Map<String, Object> createdTransaction = testContext.getDatabaseTestHelper().getTransactionByExternalId(externalTransactionId);
        assertThat(createdTransaction.get("external_id"), is(notNullValue()));
        assertThat(createdTransaction.get("reference"), is(expectedReference));
        assertThat(createdTransaction.get("description"), is(expectedDescription));
        assertThat(createdTransaction.get("amount"), is(AMOUNT));
    }

    @Test
    public void shouldCollectAPayment_forGoCardless() throws Exception {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
                .withPaymentProvider(PaymentProvider.GOCARDLESS).insert(testContext.getJdbi());
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        GoCardlessMandateId goCardlessMandate = GoCardlessMandateId.valueOf("aGoCardlessMandateId");
        Mandate mandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withPaymentProviderId(goCardlessMandate)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi())
                .toEntity();
        
        String accountExternalId = gatewayAccountFixture.getExternalId();
        String expectedReference = "Test reference";
        String expectedDescription = "Test description";
        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, AMOUNT)
                .put(JSON_REFERENCE_KEY, expectedReference)
                .put(JSON_DESCRIPTION_KEY, expectedDescription)
                .put(JSON_GATEWAY_ACC_KEY, accountExternalId)
                .put(JSON_AGREEMENT_ID_KEY, mandate.getExternalId().toString())
                .build());

        String sunName = "Test SUN Name";
        String requestPath = "/v1/api/accounts/{accountId}/charges/collect"
                .replace("{accountId}", accountExternalId);
        stubCreatePayment(gatewayAccountFixture.getAccessToken().toString(), AMOUNT, goCardlessMandate, null);
        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length() - 2);
        stubGetCreditor(gatewayAccountFixture.getAccessToken().toString(), sunName);
        // language=JSON
        String emailPayloadBody = "{\n" +
                "  \"address\": \"" + payerFixture.getEmail() + "\",\n" +
                "  \"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\",\n" +
                "  \"template\": \"ON_DEMAND_PAYMENT_CONFIRMED\",\n" +
                "  \"personalisation\": {\n" +
                "    \"amount\": \"" + BigDecimal.valueOf(AMOUNT, 2).toString() + "\",\n" +
                "    \"mandate reference\": \"" + mandate.getMandateBankStatementReference() + "\",\n" +
                "    \"bank account last 2 digits\": \"" + lastTwoDigitsBankAccount + "\",\n" +
                "    \"collection date\": \"21/05/2014\",\n" +
                "    \"statement name\": \"" + sunName + "\",\n" +
                "    \"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"\n" +
                "  }\n" +
                "}";

        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200)));

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body(JSON_CHARGE_KEY, is(notNullValue()))
                .body(JSON_AMOUNT_KEY, isNumber(AMOUNT))
                .body(JSON_REFERENCE_KEY, is(expectedReference))
                .body(JSON_DESCRIPTION_KEY, is(expectedDescription))
                .contentType(JSON);

        String externalTransactionId = response.extract().path(JSON_CHARGE_KEY).toString();

        Map<String, Object> createdTransaction = testContext.getDatabaseTestHelper().getTransactionByExternalId(externalTransactionId);
        assertThat(createdTransaction.get("external_id"), is(notNullValue()));
        assertThat(createdTransaction.get("reference"), is(expectedReference));
        assertThat(createdTransaction.get("description"), is(expectedDescription));
        assertThat(createdTransaction.get("amount"), is(AMOUNT));
    }

    @Test
    public void shouldRetrieveATransaction_fromPublicApiEndpoint() {

        String accountExternalId = testGatewayAccount.getExternalId();

        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        PaymentFixture paymentFixture = createTransactionFixtureWith(mandateFixture, PaymentState.NEW);

        String requestPath = CHARGE_API_PATH
                .replace("{accountId}", accountExternalId)
                .replace("{paymentExternalId}", paymentFixture.getExternalId());

        ValidatableResponse getChargeResponse = givenSetup()
                .get(requestPath)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body(JSON_CHARGE_KEY, is(paymentFixture.getExternalId()))
                .body(JSON_AMOUNT_KEY, isNumber(paymentFixture.getAmount()))
                .body(JSON_REFERENCE_KEY, is(paymentFixture.getReference()))
                .body(JSON_DESCRIPTION_KEY, is(paymentFixture.getDescription()))
                .body(JSON_STATE_KEY, is(paymentFixture.getState().toExternal().getState()))
                .body(JSON_RETURN_URL_KEY, is(mandateFixture.getReturnUrl()));


        String documentLocation = expectedTransactionLocationFor(accountExternalId, paymentFixture.getExternalId());
        String token = testContext.getDatabaseTestHelper().getTokenByTransactionExternalId(paymentFixture.getExternalId());

        String hrefNextUrl = "http://Frontend" + FRONTEND_CARD_DETAILS_URL + "/" + token;
        String hrefNextUrlPost = "http://Frontend" + FRONTEND_CARD_DETAILS_URL;


        getChargeResponse
                .body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", hrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", token);
                }}));

        String requestPath2 = CHARGE_API_PATH
                .replace("{accountId}", accountExternalId)
                .replace("{paymentExternalId}", paymentFixture.getExternalId());

        ValidatableResponse getChargeFromTokenResponse = givenSetup()
                .get(requestPath2)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body(JSON_CHARGE_KEY, is(paymentFixture.getExternalId()))
                .body(JSON_AMOUNT_KEY, isNumber(paymentFixture.getAmount()))
                .body(JSON_REFERENCE_KEY, is(paymentFixture.getReference()))
                .body(JSON_DESCRIPTION_KEY, is(paymentFixture.getDescription()))
                .body(JSON_STATE_KEY, is(paymentFixture.getState().toExternal().getState()))
                .body(JSON_RETURN_URL_KEY, is(mandateFixture.getReturnUrl()));

        String newChargeToken = testContext.getDatabaseTestHelper().getTokenByTransactionExternalId(paymentFixture.getExternalId());

        String newHrefNextUrl = "http://Frontend" + FRONTEND_CARD_DETAILS_URL + "/" + newChargeToken;

        getChargeFromTokenResponse
                .body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", newHrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", newChargeToken);
                }}));
    }
    
    private PaymentFixture createTransactionFixtureWith(MandateFixture mandateFixture, PaymentState paymentState) {
        return aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(paymentState)
                .insert(testContext.getJdbi());
    }

    private String expectedTransactionLocationFor(String accountId, String chargeId) {
        return "http://localhost:" + testContext.getPort() + CHARGE_API_PATH
                .replace("{accountId}", accountId)
                .replace("{paymentExternalId}", chargeId);
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
