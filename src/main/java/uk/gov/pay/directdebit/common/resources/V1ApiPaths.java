package uk.gov.pay.directdebit.common.resources;

public class V1ApiPaths {

    public static final String ROOT_PATH = "/v1";
    //have to be /charges unless we change public api
    public static final String CHARGE_API_PATH = ROOT_PATH +"/api/accounts/{accountId}/charges/{paymentRequestExternalId}";
    public static final String CHARGES_API_PATH = ROOT_PATH +"/api/accounts/{accountId}/charges";

    public static final String WEBHOOKS_GOCARDLESS_PATH = ROOT_PATH + "/webhooks/gocardless";

}
