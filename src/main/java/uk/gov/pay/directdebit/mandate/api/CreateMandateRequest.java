package uk.gov.pay.directdebit.mandate.api;

import java.util.Map;

public class CreateMandateRequest {

    private final String returnUrl, reference, description;

    private CreateMandateRequest(String returnUrl, String reference, String description) {
        this.returnUrl = returnUrl;
        this.reference = reference;
        this.description = description;
    }

    public static CreateMandateRequest of(Map<String, String> createMandateRequest) {
        return new CreateMandateRequest(createMandateRequest.get("return_url"), 
                createMandateRequest.get("service_reference"), createMandateRequest.get("description"));
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }
}
