package uk.gov.pay.directdebit.common.model;

public interface SearchParams {

    Integer getPage();
    Integer getDisplaySize();
    String getGatewayExternalId();
    String buildQueryParamString();
}
