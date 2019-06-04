package uk.gov.pay.directdebit.util;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_CUSTOMER_BANK_ACCOUNT_REQUEST;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_CUSTOMER_BANK_ACCOUNT_SUCCESS_RESPONSE;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_CUSTOMER_REQUEST;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_CUSTOMER_SUCCESS_RESPONSE;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_MANDATE_REQUEST;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_MANDATE_SUCCESS_RESPONSE;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_PAYMENT_REQUEST;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_PAYMENT_SUCCESS_RESPONSE;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_GET_CREDITOR_WITH_BACS_SUCCESS_RESPONSE;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.load;

public class GoCardlessStubs {

    public static void stubGetCreditor(String accessToken, String goCardlessSun) {
        String getCreditorResponseBody = load(GOCARDLESS_GET_CREDITOR_WITH_BACS_SUCCESS_RESPONSE)
                .replace("{{gocardless_creditor_id}}", RandomIdGenerator.newId())
                .replace("{{gocardless_sun}}", goCardlessSun);

        stubGetCallsFor("/creditors", accessToken, 200, getCreditorResponseBody);
    }

    public static void stubCreateCustomer(String accessToken, String idempotencyKey, PayerFixture payerFixture, String goCardlessCustomerId) {
        String customerRequestExpectedBody = load(GOCARDLESS_CREATE_CUSTOMER_REQUEST)
                .replace("{{email}}", payerFixture.getEmail())
                .replace("{{family_name}}", payerFixture.getName())
                .replace("{{given_name}}", payerFixture.getName());
        String customerResponseBody = load(GOCARDLESS_CREATE_CUSTOMER_SUCCESS_RESPONSE)
                .replace("{{email}}", payerFixture.getEmail())
                .replace("{{family_name}}", payerFixture.getName())
                .replace("{{given_name}}", payerFixture.getName())
                .replace("{{gocardless_customer_id}}", goCardlessCustomerId);

        stubPostCallsFor("/customers", accessToken, 200, idempotencyKey, customerRequestExpectedBody, customerResponseBody);
    }

    public static void stubCreateCustomerBankAccount(String accessToken, String idempotencyKey, PayerFixture payerFixture, String goCardlessCustomerId, String goCardlessBankAccountId) {
        String customerBankAccountRequestExpectedBody = load(GOCARDLESS_CREATE_CUSTOMER_BANK_ACCOUNT_REQUEST)
                .replace("{{account_holder_name}}", payerFixture.getName())
                .replace("{{account_number}}", payerFixture.getAccountNumber())
                .replace("{{sort_code}}", payerFixture.getSortCode())
                .replace("{{gocardless_customer_id}}", goCardlessCustomerId);

        String customerBankAccountResponseBody = load(GOCARDLESS_CREATE_CUSTOMER_BANK_ACCOUNT_SUCCESS_RESPONSE)
                .replace("{{account_holder_name}}", payerFixture.getName())
                .replace("{{gocardless_customer_id}}", goCardlessCustomerId)
                .replace("{{gocardless_customer_bank_account_id}}", goCardlessBankAccountId);
        stubPostCallsFor("/customer_bank_accounts", accessToken, 200, idempotencyKey, customerBankAccountRequestExpectedBody, customerBankAccountResponseBody);
    }

    public static void stubCreateMandate(String accessToken, String idempotencyKey, GoCardlessCustomerFixture goCardlessCustomerFixture) {
        String mandateRequestExpectedBody = load(GOCARDLESS_CREATE_MANDATE_REQUEST)
                .replace("{{customer_bank_account_id}}", goCardlessCustomerFixture.getCustomerBankAccountId());

        String mandateResponseBody = load(GOCARDLESS_CREATE_MANDATE_SUCCESS_RESPONSE)
                .replace("{{customer_bank_account_id}}", goCardlessCustomerFixture.getCustomerBankAccountId())
                .replace("{{customer_id}}", goCardlessCustomerFixture.getCustomerId())
                .replace("{{gocardless_customer_bank_account_id}}", goCardlessCustomerFixture.getCustomerBankAccountId());
        stubPostCallsFor("/mandates", accessToken, 200, idempotencyKey, mandateRequestExpectedBody, mandateResponseBody);
    }

    public static void stubCreatePayment(String accessToken, Long amount, GoCardlessMandateId goCardlessMandateId, String idempotencyKey) {
        String paymentRequestExpectedBody = load(GOCARDLESS_CREATE_PAYMENT_REQUEST)
                .replace("{{amount}}", String.valueOf(amount))
                .replace("{{gocardless_mandate_id}}", goCardlessMandateId.toString());

        String paymentResponseBody = load(GOCARDLESS_CREATE_PAYMENT_SUCCESS_RESPONSE)
                .replace("{{amount}}", String.valueOf(amount))
                .replace("{{gocardless_mandate_id}}", goCardlessMandateId.toString());
        stubPostCallsFor("/payments", accessToken, 200, idempotencyKey, paymentRequestExpectedBody, paymentResponseBody);
    }

    private static void stubGetCallsFor(String url, String accessToken, int statusCode, String responseBody) {
        MappingBuilder getRequest = get(urlPathEqualTo(url));
        getRequest
                .withHeader("Authorization", equalTo("Bearer " + accessToken))
                .willReturn(
                        aResponse()
                                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                .withStatus(statusCode)
                                .withBody(responseBody)
                );
        stubFor(getRequest);
    }

    private static void stubPostCallsFor(String url, String accessToken, int statusCode, String idempotencyKey, String requestBody, String responseBody) {
        MappingBuilder postRequest = post(urlPathEqualTo(url));
        if (idempotencyKey != null) {
            postRequest.withHeader("Idempotency-Key", equalTo(idempotencyKey));
        }
        postRequest
                .withHeader("Authorization", equalTo("Bearer " + accessToken))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(
                        aResponse()
                                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                .withStatus(statusCode)
                                .withBody(responseBody)
                );
        stubFor(postRequest);
    }

}
