package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExternalMandateState {
    CREATED("created", false),
    STARTED("started", false),
    PENDING("pending", false),
    SUBMITTED("submitted", false),
    ACTIVE("active", true),
    INACTIVE("inactive", true),
    CANCELLED("cancelled", true);

    private final String value;
    private final boolean finished;
    
    ExternalMandateState(String value, boolean finished) {
        this.value = value;
        this.finished = finished;
    }

    @JsonProperty("status")
    public String getState() {
        return value;
    }

    public boolean isFinished() {
        return finished;
    }
}
