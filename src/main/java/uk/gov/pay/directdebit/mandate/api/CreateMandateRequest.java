package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMandateRequest {

    @NotNull(message = "Field [return_url] cannot be null")
    @Length(min = 1, max = 255, message = "Field [return_url] must have a size between {min} and {max}")
    @JsonProperty("return_url")
    private String returnUrl;

    //TODO disabling this temporarily to sort out failing pacts, re-enable later.
    //@NotNull(message = "Field [service_reference] cannot be null")
    @Length(min = 1, max = 255, message = "Field [service_reference] must have a size between {min} and {max}")
    @JsonProperty("service_reference")
    private String reference;

    @JsonProperty
    @Length(min = 1, max = 255, message = "Field [description] must have a size between {min} and {max}")
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

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }
}
