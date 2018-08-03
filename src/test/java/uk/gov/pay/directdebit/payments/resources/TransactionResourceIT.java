package uk.gov.pay.directdebit.payments.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang.RandomStringUtils;
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
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.payments.resources.TransactionResource.CHARGES_API_PATH;
import static uk.gov.pay.directdebit.payments.resources.TransactionResource.CHARGE_API_PATH;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreatePayment;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubGetCreditor;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;
import static uk.gov.pay.directdebit.util.ResponseContainsLinkMatcher.containsLink;


@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class TransactionResourceIT {

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
    public void shouldCreateAMandateAndATransactionForOneOffTransaction() throws Exception {
        String accountExternalId = testGatewayAccount.getExternalId();
        String expectedReference = "Test reference";
        String expectedDescription = "Test description";
        String returnUrl = "http://service.url/success-page/";
        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, AMOUNT)
                .put(JSON_REFERENCE_KEY, expectedReference)
                .put(JSON_DESCRIPTION_KEY, expectedDescription)
                .put(JSON_GATEWAY_ACC_KEY, accountExternalId)
                .put(JSON_RETURN_URL_KEY, returnUrl)
                .build());

        String requestPath = CHARGES_API_PATH
                .replace("{accountId}", accountExternalId);

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body(JSON_CHARGE_KEY, is(notNullValue()))
                .body(JSON_AMOUNT_KEY, isNumber(AMOUNT))
                .body(JSON_REFERENCE_KEY, is(expectedReference))
                .body(JSON_DESCRIPTION_KEY, is(expectedDescription))
                .body(JSON_RETURN_URL_KEY, is(returnUrl))
                .contentType(JSON);

        String externalTransactionId = response.extract().path(JSON_CHARGE_KEY).toString();

        Map<String, Object> createdTransaction = testContext.getDatabaseTestHelper().getTransactionByExternalId(externalTransactionId);
        assertThat(createdTransaction.get("external_id"), is(notNullValue()));
        assertThat(createdTransaction.get("reference"), is(expectedReference));
        assertThat(createdTransaction.get("description"), is(expectedDescription));
        assertThat(createdTransaction.get("amount"), is(AMOUNT));

        Map<String, Object> createdMandate = testContext.getDatabaseTestHelper().getMandateByTransactionExternalId(externalTransactionId);

        assertThat(createdMandate.get("external_id"), is(notNullValue()));
        assertThat(createdMandate.get("reference"), is(expectedReference));
        assertThat(createdMandate.get("description"), is(expectedDescription));
        assertThat(createdMandate.get("return_url"), is(returnUrl));
        assertThat(createdMandate.get("gateway_account_id"), is(testGatewayAccount.getId()));
        assertThat(createdMandate.get("payer"), is(nullValue()));
    }

    @Test
    public void shouldCollectAPayment_forSandbox() throws Exception {
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .withMandateType(MandateType.ON_DEMAND)
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
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ON_DEMAND)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());
        GoCardlessMandate goCardlessMandate = GoCardlessMandateFixture.aGoCardlessMandateFixture()
                .withMandateId(mandateFixture.getId())
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
                .put(JSON_AGREEMENT_ID_KEY, mandateFixture.getExternalId().toString())
                .build());

        String sunName = "Test SUN Name";
        String requestPath = "/v1/api/accounts/{accountId}/charges/collect"
                .replace("{accountId}", accountExternalId);
        stubCreatePayment(gatewayAccountFixture.getAccessToken().toString(), AMOUNT, goCardlessMandate.getGoCardlessMandateId(), null);
        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length() - 2);
        stubGetCreditor(gatewayAccountFixture.getAccessToken().toString(), goCardlessMandate.getGoCardlessCreditorId().toString(), sunName);
        // language=JSON
        String emailPayloadBody = "{\n" +
                "  \"address\": \"" + payerFixture.getEmail() + "\",\n" +
                "  \"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\",\n" +
                "  \"template\": \"ON_DEMAND_PAYMENT_CONFIRMED\",\n" +
                "  \"personalisation\": {\n" +
                "    \"amount\": \"" + BigDecimal.valueOf(AMOUNT, 2).toString() + "\",\n" +
                "    \"mandate reference\": \"" + mandateFixture.getMandateReference() + "\",\n" +
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
    public void shouldNotCollectAPaymentFromOneOff() throws Exception {
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .withMandateType(MandateType.ONE_OFF)
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

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .body("message", containsString("Invalid operation"))
                .statusCode(Status.PRECONDITION_FAILED.getStatusCode())
                .contentType(JSON);
    }

    @Test
    public void shouldRetrieveATransaction_fromPublicApiEndpoint() {

        String accountExternalId = testGatewayAccount.getExternalId();

        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = createTransactionFixtureWith(mandateFixture, PaymentState.NEW);


        String requestPath = CHARGE_API_PATH
                .replace("{accountId}", accountExternalId)
                .replace("{transactionExternalId}", transactionFixture.getExternalId());

        ValidatableResponse getChargeResponse = givenSetup()
                .get(requestPath)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body(JSON_CHARGE_KEY, is(transactionFixture.getExternalId()))
                .body(JSON_AMOUNT_KEY, isNumber(transactionFixture.getAmount()))
                .body(JSON_REFERENCE_KEY, is(transactionFixture.getReference()))
                .body(JSON_DESCRIPTION_KEY, is(transactionFixture.getDescription()))
                .body(JSON_STATE_KEY, is(transactionFixture.getState().toExternal().getState()))
                .body(JSON_RETURN_URL_KEY, is(mandateFixture.getReturnUrl()));


        String documentLocation = expectedTransactionLocationFor(accountExternalId, transactionFixture.getExternalId());
        String token = testContext.getDatabaseTestHelper().getTokenByTransactionExternalId(transactionFixture.getExternalId());

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
                .replace("{transactionExternalId}", transactionFixture.getExternalId());

        ValidatableResponse getChargeFromTokenResponse = givenSetup()
                .get(requestPath2)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body(JSON_CHARGE_KEY, is(transactionFixture.getExternalId()))
                .body(JSON_AMOUNT_KEY, isNumber(transactionFixture.getAmount()))
                .body(JSON_REFERENCE_KEY, is(transactionFixture.getReference()))
                .body(JSON_DESCRIPTION_KEY, is(transactionFixture.getDescription()))
                .body(JSON_STATE_KEY, is(transactionFixture.getState().toExternal().getState()))
                .body(JSON_RETURN_URL_KEY, is(mandateFixture.getReturnUrl()));

        String newChargeToken = testContext.getDatabaseTestHelper().getTokenByTransactionExternalId(transactionFixture.getExternalId());

        String newHrefNextUrl = "http://Frontend" + FRONTEND_CARD_DETAILS_URL + "/" + newChargeToken;

        getChargeFromTokenResponse
                .body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", newHrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", newChargeToken);
                }}));
    }

    @Test
    public void shouldReturn400IfMandatoryFieldsMissing() throws JsonProcessingException {
        String accountId = testGatewayAccount.getExternalId();

        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, AMOUNT)
                .put(JSON_DESCRIPTION_KEY, "desc")
                .put(JSON_GATEWAY_ACC_KEY, accountId)
                .put(JSON_RETURN_URL_KEY, "http://service.url/success-page/")
                .build());
        String requestPath = CHARGES_API_PATH
                .replace("{accountId}", accountId);

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Field(s) missing: [reference]"));
    }

    @Test
    public void shouldReturn400IfFieldsInvalidSize() throws JsonProcessingException {
        String accountId = testGatewayAccount.getExternalId();

        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, AMOUNT)
                .put(JSON_REFERENCE_KEY, "reference")
                .put(JSON_DESCRIPTION_KEY, RandomStringUtils.randomAlphabetic(256))
                .put(JSON_GATEWAY_ACC_KEY, accountId)
                .put(JSON_RETURN_URL_KEY, "http://service.url/success-page/")
                .build());
        String requestPath = CHARGES_API_PATH
                .replace("{accountId}", accountId);

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("The size of a field(s) is invalid: [description]"));
    }

    @Test
    public void shouldReturn400IfFieldsInvalid() throws JsonProcessingException {
        String accountId = testGatewayAccount.getExternalId();
        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, 10000001)
                .put(JSON_REFERENCE_KEY, "reference")
                .put(JSON_DESCRIPTION_KEY, "desc")
                .put(JSON_GATEWAY_ACC_KEY, accountId)
                .put(JSON_RETURN_URL_KEY, "http://service.url/success-page/")
                .build());
        String requestPath = CHARGES_API_PATH
                .replace("{accountId}", accountId);

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Field(s) are invalid: [amount]"));
    }

    private TransactionFixture createTransactionFixtureWith(MandateFixture mandateFixture, PaymentState paymentState) {
        return aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(paymentState)
                .insert(testContext.getJdbi());
    }

    private String expectedTransactionLocationFor(String accountId, String chargeId) {
        return "http://localhost:" + testContext.getPort() + CHARGE_API_PATH
                .replace("{accountId}", accountId)
                .replace("{transactionExternalId}", chargeId);
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
