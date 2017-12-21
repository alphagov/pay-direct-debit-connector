package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExternalPaymentState {
    EXTERNAL_STARTED("started", false),
    EXTERNAL_PENDING("pending", false),
    EXTERNAL_SUCCESS("success", true),
    EXTERNAL_FAILED("failed", true),
    EXTERNAL_CANCELLED("cancelled", true),
    EXTERNAL_EXPIRED("expired", true);

    private final String value;
    private final boolean finished;

    ExternalPaymentState(String value, boolean finished) {
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

