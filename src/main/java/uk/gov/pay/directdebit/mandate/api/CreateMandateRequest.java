package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMandateRequest {

    @NotNull(message = "Field [return_url] cannot be null")
    @Length(min = 1, max = 255, message = "Field [return_url] must have a size between 1 and 255")
    @JsonProperty("return_url")
    private String returnUrl;

    @NotNull(message = "Field [service_reference] cannot be null")
    @Length(min = 1, max = 255, message = "Field [service_reference] must have a size between 1 and 255")
    @JsonProperty("service_reference")
    private String reference;

    @JsonProperty
    @Length(min = 1, max = 255, message = "Field [description] must have a size between 1 and 255")
    private String description;

    public CreateMandateRequest() {}

    public CreateMandateRequest(String returnUrl, String reference) {
        this.returnUrl = returnUrl;
        this.reference = reference;
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
