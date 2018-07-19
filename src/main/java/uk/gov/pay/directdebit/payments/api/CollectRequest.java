package uk.gov.pay.directdebit.payments.api;

public interface CollectRequest {

    String getReference();

    Long getAmount();
    
    String getDescription();
}
