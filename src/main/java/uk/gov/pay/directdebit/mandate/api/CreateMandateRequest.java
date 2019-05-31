package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class CreateMandateRequest {

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("service_reference")
    private String reference;

    private CreateMandateRequest(String returnUrl, String reference) {
        this.returnUrl = returnUrl;
        this.reference = reference;
    }

    public static CreateMandateRequest of(Map<String, String> createMandateRequest) {
        return new CreateMandateRequest(createMandateRequest.get("return_url"), 
                createMandateRequest.get("service_reference"));
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getReference() {
        return reference;
    }
}
