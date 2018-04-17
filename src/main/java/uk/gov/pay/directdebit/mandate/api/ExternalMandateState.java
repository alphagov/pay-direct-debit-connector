package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExternalMandateState {
    EXTERNAL_PENDING("pending"),
    EXTERNAL_ACTIVE("active"),
    EXTERNAL_INACTIVE("inactive");

    private final String value;

    ExternalMandateState(String value) {
        this.value = value;
    }

    @JsonProperty("status")
    public String getState() {
        return value;
    }
}
