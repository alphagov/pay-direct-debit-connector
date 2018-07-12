package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import uk.gov.pay.directdebit.mandate.model.MandateType;

public class CreateMandateRequest implements CreateRequest {

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("agreement_type")
    private MandateType mandateType;

    @JsonProperty("service_reference")
    private String reference;

    private CreateMandateRequest(String returnUrl,
            MandateType mandateType, String reference) {
        this.returnUrl = returnUrl;
        this.mandateType = mandateType;
        this.reference = reference;
    }

    public static CreateMandateRequest of(Map<String, String> createMandateRequest) {
        return new CreateMandateRequest(
                createMandateRequest.get("return_url"),
                MandateType.fromString(createMandateRequest.get("agreement_type")),
                createMandateRequest.get("service_reference")
        );
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public MandateType getMandateType() {
        return mandateType;
    }

    public String getReference() {
        return reference;
    }
}
