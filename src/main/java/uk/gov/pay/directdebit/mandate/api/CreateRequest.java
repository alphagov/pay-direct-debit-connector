package uk.gov.pay.directdebit.mandate.api;

import uk.gov.pay.directdebit.mandate.model.MandateType;

public interface CreateRequest {
    
    String getReference();
    
    String getReturnUrl();
    
    MandateType getMandateType();
}
