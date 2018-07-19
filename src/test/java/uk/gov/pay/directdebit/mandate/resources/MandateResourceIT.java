package uk.gov.pay.directdebit.mandate.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import javax.ws.rs.core.Response.Status;
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
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture.aGoCardlessCustomerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateCustomer;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateCustomerBankAccount;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateMandate;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreatePayment;
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

    @Before
    public void setUp() {
        wireMockAdminUsers.start();
        gatewayAccountFixture.insert(testContext.getJdbi());
    }

    @Test
    public void shouldCreateAMandateWithoutTransaction_IfMandateIsOnDemand() throws Exception {
        String accountExternalId = gatewayAccountFixture.getExternalId();
        String agreementType = MandateType.ON_DEMAND.toString();
        String returnUrl = "http://example.com/success-page/";
        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put("agreement_type", agreementType)
                .put("return_url", returnUrl)
                .build());

        String requestPath = "/v1/api/accounts/{accountId}/mandates"
                .replace("{accountId}", accountExternalId);

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body(JSON_MANDATE_ID_KEY, is(notNullValue()))
                .body("mandate_type", is(agreementType))
                .body("return_url", is(returnUrl))
                .body("created_date", is(notNullValue()))
                .contentType(JSON);
        String externalMandateId = response.extract().path(JSON_MANDATE_ID_KEY).toString();

        String documentLocation = expectedMandateLocationFor(accountExternalId, externalMandateId);
        String token = testContext.getDatabaseTestHelper().getTokenByMandateExternalId(externalMandateId).get("secure_redirect_token").toString();

        String hrefNextUrl = "http://Frontend/secure/" + token;
        String hrefNextUrlPost = "http://Frontend/secure";

        response
                .body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", hrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", token);
                }}));

        Map<String, Object> createdMandate = testContext.getDatabaseTestHelper().getMandateByExternalId(externalMandateId);

        assertThat(createdMandate.get("external_id"), is(notNullValue()));
        assertThat(createdMandate.get("return_url"), is(returnUrl));
        assertThat(createdMandate.get("gateway_account_id"), is(gatewayAccountFixture.getId()));
        assertThat(createdMandate.get("payer"), is(nullValue()));
        assertThat(createdMandate.get("transaction"), is(nullValue()));
    }

    @Test
    public void shouldCreateAMandate_withAllFields() throws Exception {
        String accountExternalId = gatewayAccountFixture.getExternalId();
        String agreementType = MandateType.ON_DEMAND.toString();
        String returnUrl = "http://example.com/success-page/";
        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put("agreement_type", agreementType)
                .put("return_url", returnUrl)
                .put("service_reference", "test-service-reference")
                .build());

        String requestPath = "/v1/api/accounts/{accountId}/mandates"
                .replace("{accountId}", accountExternalId);

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body(JSON_MANDATE_ID_KEY, is(notNullValue()))
                .body("mandate_type", is(agreementType))
                .body("return_url", is(returnUrl))
                .body("created_date", is(notNullValue()))
                .body("state.status", is("created"))
                .body("state.finished", is(false))
                .body("service_reference", is("test-service-reference"))
                .body("mandate_reference", is(notNullValue()))
                .contentType(JSON);
        String externalMandateId = response.extract().path(JSON_MANDATE_ID_KEY).toString();

        String documentLocation = expectedMandateLocationFor(accountExternalId, externalMandateId);
        String token = testContext.getDatabaseTestHelper().getTokenByMandateExternalId(externalMandateId).get("secure_redirect_token").toString();

        String hrefNextUrl = "http://Frontend/secure/" + token;
        String hrefNextUrlPost = "http://Frontend/secure";

        response
                .body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", hrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", token);
                }}));
    }

    @Test
    public void shouldNotCreateAOneOffMandate() throws Exception {
        String accountExternalId = gatewayAccountFixture.getExternalId();
        String agreementType = MandateType.ONE_OFF.toString();
        String returnUrl = "http://example.com/success-page/";
        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put("agreement_type", agreementType)
                .put("return_url", returnUrl)
                .put("service_reference", "test-service-reference")
                .build());

        String requestPath = "/v1/api/accounts/{accountId}/mandates"
                .replace("{accountId}", accountExternalId);

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Status.PRECONDITION_FAILED.getStatusCode())
                .contentType(JSON)
                .body("message", is("Invalid operation on mandate of type ONE_OFF"));
    }
    
    @Test
    public void shouldRetrieveAMandate_FromFrontendEndpoint_WhenATransactionHasBeenCreated() {
        String accountExternalId = gatewayAccountFixture.getExternalId();
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withPayerFixture(payerFixture)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = createTransactionFixtureWith(mandateFixture, PaymentState.NEW);

        String frontendMandateWithTransactionPath = "/v1/accounts/{accountId}/mandates/{mandateExternalId}/payments/{transactionExternalId}";
        String requestPath = frontendMandateWithTransactionPath
                .replace("{accountId}", accountExternalId)
                .replace("{mandateExternalId}", mandateFixture.getExternalId())
                .replace("{transactionExternalId}", transactionFixture.getExternalId());

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body("external_id", is(mandateFixture.getExternalId()))
                .body("gateway_account_id", isNumber(gatewayAccountFixture.getId()))
                .body("gateway_account_external_id", is(gatewayAccountFixture.getExternalId()))
                .body("state.status", is(mandateFixture.getState().toExternal().getState()))
                .body("internal_state", is(mandateFixture.getState().toString()))
                .body("mandate_reference", is(mandateFixture.getMandateReference()))
                .body("mandate_type", is(mandateFixture.getMandateType().toString()))
                .body("created_date", is(mandateFixture.getCreatedDate().toString()))
                .body("transaction." + JSON_AMOUNT_KEY, isNumber(transactionFixture.getAmount()))
                .body("transaction." + JSON_REFERENCE_KEY, is(transactionFixture.getReference()))
                .body("transaction." + JSON_DESCRIPTION_KEY, is(transactionFixture.getDescription()))
                .body("transaction." + JSON_STATE_KEY, is(transactionFixture.getState().toExternal().getState()))
                .body("payer.payer_external_id", is(payerFixture.getExternalId()))
                .body("payer.account_holder_name", is(payerFixture.getName()))
                .body("payer.email", is(payerFixture.getEmail()))
                .body("payer.requires_authorisation", is(payerFixture.getAccountRequiresAuthorisation()));
    }

    @Test
    public void shouldRetrieveAMandate_FromFrontendEndpoint_WhenNoTransactionHasBeenCreated() {

        String accountExternalId = gatewayAccountFixture.getExternalId();
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withPayerFixture(payerFixture)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        String frontendMandatePath = "/v1/accounts/{accountId}/mandates/{mandateExternalId}";
        String requestPath = frontendMandatePath
                .replace("{accountId}", accountExternalId)
                .replace("{mandateExternalId}", mandateFixture.getExternalId());

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body("external_id", is(mandateFixture.getExternalId()))
                .body("gateway_account_id", isNumber(gatewayAccountFixture.getId()))
                .body("gateway_account_external_id", is(gatewayAccountFixture.getExternalId()))
                .body("state.status", is(mandateFixture.getState().toExternal().getState()))
                .body("internal_state", is(mandateFixture.getState().toString()))
                .body("mandate_reference", is(mandateFixture.getMandateReference()))
                .body("mandate_type", is(mandateFixture.getMandateType().toString()))
                .body("created_date", is(mandateFixture.getCreatedDate().toString()))
                .body("$", not(hasKey("transaction")))
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
                .replace("{mandateExternalId}", mandateFixture.getExternalId());

        ValidatableResponse getMandateResponse = givenSetup()
                .get(requestPath)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body("mandate_id", is(mandateFixture.getExternalId()))
                .body("mandate_type", is(mandateFixture.getMandateType().toString()))
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
                .withMandateType(MandateType.ON_DEMAND)
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/cancel"
                .replace("{accountId}", gatewayAccountFixture.getExternalId())
                .replace("{mandateExternalId}", mandateFixture.getExternalId());

        givenSetup()
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(transaction.get("state"), is("CANCELLED"));
    }

    @Test
    public void shouldCancelAMandateAndTransaction_ifMandateIsOneOff() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ONE_OFF)
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());

        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/cancel"
                .replace("{accountId}", gatewayAccountFixture.getExternalId())
                .replace("{mandateExternalId}", mandateFixture.getExternalId());

        givenSetup()
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(mandate.get("state"), is("CANCELLED"));
        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        assertThat(transaction.get("state"), is("CANCELLED"));
    }

    @Test
    public void shouldChangePaymentType() {
        MandateFixture testMandate = MandateFixture.aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withMandateType(MandateType.ON_DEMAND)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/change-payment-method"
                .replace("{accountId}", gatewayAccountFixture.getExternalId())
                .replace("{mandateExternalId}", testMandate.getExternalId());

        givenSetup()
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getMandateById(testMandate.getId());
        assertThat(transaction.get("state"), is("USER_CANCEL_NOT_ELIGIBLE"));
    }

    @Test
    public void shouldChangePaymentType_ifMandateIsOneOff() {
        MandateFixture testMandate = MandateFixture.aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withMandateType(MandateType.ONE_OFF)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = aTransactionFixture()
                .withMandateFixture(testMandate)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());

        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/change-payment-method"
                .replace("{accountId}", gatewayAccountFixture.getExternalId())
                .replace("{mandateExternalId}", testMandate.getExternalId());

        givenSetup()
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(testMandate.getId());
        assertThat(mandate.get("state"), is("USER_CANCEL_NOT_ELIGIBLE"));
        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        assertThat(transaction.get("state"), is("USER_CANCEL_NOT_ELIGIBLE"));
    }

    @Test
    public void confirm_shouldCreateAMandateAndUpdateCharge_ifMandateIsOneOff() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ONE_OFF)
                .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());

        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length() - 2);
        String chargeDate = LocalDate.now().plusDays(4).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // language=JSON
        String emailPayloadBody = "{\n" +
                "  \"address\": \"" + payerFixture.getEmail() + "\",\n" +
                "  \"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\",\n" +
                "  \"template\": \"ONE_OFF_PAYMENT_CONFIRMED\",\n" +
                "  \"personalisation\": {\n" +
                "    \"amount\": \"" + BigDecimal.valueOf(transactionFixture.getAmount(), 2).toString() + "\",\n" +
                "    \"mandate reference\": \"" + mandateFixture.getMandateReference() + "\",\n" +
                "    \"bank account last 2 digits\": \"" + lastTwoDigitsBankAccount + "\",\n" +
                "    \"collection date\": \"" + chargeDate + "\",\n" +
                "    \"statement name\": \"THE-CAKE-IS-A-LIE\",\n" +
                "    \"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"\n" +
                "  }\n" +
                "}";

        // language=JSON
        String confirmDetails = "{\n" +
                "  \"sort_code\": \"" + payerFixture.getSortCode() + "\",\n" +
                "  \"account_number\": \"" + payerFixture.getAccountNumber() + "\",\n" +
                "  \"transaction_external_id\": \"" + transactionFixture.getExternalId() + "\"\n" +
                "}\n";

        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200)));

        String requestPath = String.format("/v1/api/accounts/%s/mandates/%s/confirm", gatewayAccountFixture.getExternalId(), mandateFixture.getExternalId());
        given().port(testContext.getPort())
                .body(confirmDetails)
                .contentType(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        List<Map<String, Object>> transactionsForMandate = testContext.getDatabaseTestHelper().getTransactionsForMandate(mandateFixture.getExternalId());
        MatcherAssert.assertThat(transactionsForMandate.size(), is(1));
        MatcherAssert.assertThat(transactionsForMandate.get(0).get("state"), is("SUBMITTED"));
    }

    @Test
    public void confirm_shouldCreateAMandateWithoutTransaction_ifMandateIsOnDemand() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ON_DEMAND)
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
                "    \"statement name\": \"THE-CAKE-IS-A-LIE\",\n" +
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

        String requestPath = String.format("/v1/api/accounts/%s/mandates/%s/confirm", gatewayAccountFixture.getExternalId(), mandateFixture.getExternalId());
        given().port(testContext.getPort())
                .body(confirmDetails)
                .contentType(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        List<Map<String, Object>> transactionsForMandate = testContext.getDatabaseTestHelper().getTransactionsForMandate(mandateFixture.getExternalId());
        MatcherAssert.assertThat(transactionsForMandate, is(empty()));
    }

    @Test
    public void confirm_shouldCreateACustomerBankAccountMandateAndUpdateCharge_ForGoCardless_ifMandateIsOneOff() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS).insert(testContext.getJdbi());
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ONE_OFF)
                .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());

        String customerId = "CU000358S3A2FP";
        String customerBankAccountId = "BA0002WR3Z193A";
        GoCardlessCustomerFixture goCardlessCustomerFixture = aGoCardlessCustomerFixture().
                withCustomerId(customerId)
                .withCustomerBankAccountId(customerBankAccountId)
                .withPayerId(payerFixture.getId());
        stubCreateCustomer(mandateFixture.getExternalId(), payerFixture, customerId);
        stubCreateCustomerBankAccount(mandateFixture.getExternalId(), payerFixture, customerId, customerBankAccountId);
        stubCreateMandate(mandateFixture.getExternalId(), goCardlessCustomerFixture);
        stubCreatePayment(transactionFixture.getAmount(), "MD123", transactionFixture.getExternalId());

        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length() - 2);

        // language=JSON
        String emailPayloadBody = "{\n" +
                "  \"address\": \"" + payerFixture.getEmail() + "\",\n" +
                "  \"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\",\n" +
                "  \"template\": \"ONE_OFF_PAYMENT_CONFIRMED\",\n" +
                "  \"personalisation\": {\n" +
                "    \"amount\": \"" + BigDecimal.valueOf(transactionFixture.getAmount(), 2).toString() + "\",\n" +
                "    \"mandate reference\": \"" + mandateFixture.getMandateReference() + "\",\n" +
                "    \"bank account last 2 digits\": \"" + lastTwoDigitsBankAccount + "\",\n" +
                "    \"collection date\": \"2014-05-21\",\n" +
                "    \"statement name\": \"THE-CAKE-IS-A-LIE\",\n" +
                "    \"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"\n" +
                "  }\n" +
                "}\n";

        // language=JSON
        String confirmDetails = "{\n" +
                "  \"sort_code\": \"" + payerFixture.getSortCode() + "\",\n" +
                "  \"account_number\": \"" + payerFixture.getAccountNumber() + "\",\n" +
                "  \"transaction_external_id\": \"" + transactionFixture.getExternalId() + "\"\n" +
                "}\n";

        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200)));

        String requestPath = String.format("/v1/api/accounts/%s/mandates/%s/confirm",
                gatewayAccountFixture.getExternalId(), mandateFixture.getExternalId());
        given().port(testContext.getPort())
                .contentType(APPLICATION_JSON)
                .body(confirmDetails)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        List<Map<String, Object>> transactionsForMandate = testContext.getDatabaseTestHelper().getTransactionsForMandate(mandateFixture.getExternalId());
        MatcherAssert.assertThat(transactionsForMandate.size(), is(1));
        MatcherAssert.assertThat(transactionsForMandate.get(0).get("state"), is("SUBMITTED"));
    }

    @Test
    public void confirm_shouldCreateACustomerBankAccountMandateWithNoTransaction_ForGoCardless_ifMandateIsOnDemand() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS).insert(testContext.getJdbi());
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ON_DEMAND)
                .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());

        String customerId = "CU000358S3A2FP";
        String customerBankAccountId = "BA0002WR3Z193A";
        GoCardlessCustomerFixture goCardlessCustomerFixture = aGoCardlessCustomerFixture().
                withCustomerId(customerId)
                .withCustomerBankAccountId(customerBankAccountId)
                .withPayerId(payerFixture.getId());
        stubCreateCustomer(mandateFixture.getExternalId(), payerFixture, customerId);
        stubCreateCustomerBankAccount(mandateFixture.getExternalId(), payerFixture, customerId, customerBankAccountId);
        stubCreateMandate(mandateFixture.getExternalId(), goCardlessCustomerFixture);

        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length()-2);
        // language=JSON
        String emailPayloadBody = "{\n" +
                "  \"address\": \"" + payerFixture.getEmail() + "\",\n" +
                "  \"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\",\n" +
                "  \"template\": \"ON_DEMAND_MANDATE_CREATED\",\n" +
                "  \"personalisation\": {\n" +
                "    \"mandate reference\": \"" + mandateFixture.getMandateReference() + "\",\n" +
                "    \"bank account last 2 digits\": \"" + lastTwoDigitsBankAccount + "\",\n" +
                "    \"statement name\": \"THE-CAKE-IS-A-LIE\",\n" +
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

        String requestPath = String.format("/v1/api/accounts/%s/mandates/%s/confirm",
                gatewayAccountFixture.getExternalId(), mandateFixture.getExternalId());
        given().port(testContext.getPort())
                .contentType(APPLICATION_JSON)
                .body(confirmDetails)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        List<Map<String, Object>> transactionsForMandate = testContext.getDatabaseTestHelper().getTransactionsForMandate(mandateFixture.getExternalId());
        MatcherAssert.assertThat(transactionsForMandate, is(empty()));
    }
    
    private TransactionFixture createTransactionFixtureWith(MandateFixture mandateFixture, PaymentState paymentState) {
        return aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(paymentState)
                .insert(testContext.getJdbi());
    }

    private String expectedMandateLocationFor(String accountId, String mandateExternalId) {
        return "http://localhost:" + testContext.getPort() + "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}"
                .replace("{accountId}", accountId)
                .replace("{mandateExternalId}", mandateExternalId);
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }

}
