package uk.gov.pay.directdebit.util;

import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.Charset;

public class TestRequestResponsesLoader {
    private static final String RESPONSES_BASE_NAME = "it/responses";
    private static final String REQUESTS_BASE_NAME = "it/requests";

    private static final String RESPONSES_GOCARLDESS_BASE_NAME = RESPONSES_BASE_NAME + "/gocardless";
    private static final String REQUESTS_GOCARLDESS_BASE_NAME = REQUESTS_BASE_NAME + "/gocardless";

    //requests
    public static final String GOCARDLESS_CREATE_CUSTOMER_REQUEST = REQUESTS_GOCARLDESS_BASE_NAME + "/create-customer.json";
    public static final String GOCARDLESS_CREATE_CUSTOMER_BANK_ACCOUNT_REQUEST = REQUESTS_GOCARLDESS_BASE_NAME + "/create-customer-bank-account.json";
    public static final String GOCARDLESS_CREATE_MANDATE_REQUEST = REQUESTS_GOCARLDESS_BASE_NAME + "/create-mandate.json";
    public static final String GOCARDLESS_CREATE_PAYMENT_REQUEST = REQUESTS_GOCARLDESS_BASE_NAME + "/create-payment.json";

    //responses
    public static final String GOCARDLESS_CREATE_CUSTOMER_SUCCESS_RESPONSE = RESPONSES_GOCARLDESS_BASE_NAME + "/create-customer-success.json";
    public static final String GOCARDLESS_CREATE_CUSTOMER_BANK_ACCOUNT_SUCCESS_RESPONSE = RESPONSES_GOCARLDESS_BASE_NAME + "/create-customer-bank-account-success.json";
    public static final String GOCARDLESS_CREATE_MANDATE_SUCCESS_RESPONSE = RESPONSES_GOCARLDESS_BASE_NAME + "/create-mandate-success.json";
    public static final String GOCARDLESS_CREATE_PAYMENT_SUCCESS_RESPONSE = RESPONSES_GOCARLDESS_BASE_NAME + "/create-payment-success.json";

    static public String load(String location) {
        try {
            return Resources.toString(Resources.getResource(location), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Could not load template", e);
        }
    }

}

