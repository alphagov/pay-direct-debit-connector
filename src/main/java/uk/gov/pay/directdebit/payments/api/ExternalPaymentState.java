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
    EXTERNAL_EXPIRED("expired", true),
    EXTERNAL_CANCELLED_USER_NOT_ELIGIBLE("cancelled", true, "P0060", "User not eligible for Direct Debit");

    private final String value;
    private final boolean finished;
    private final String code;
    private final String message;

    ExternalPaymentState(String value, boolean finished) {
        this.value = value;
        this.finished = finished;
        this.code = null;
        this.message = null;
    }

    ExternalPaymentState(String value, boolean finished, String code, String message) {
        this.value = value;
        this.finished = finished;
        this.code = code;
        this.message = message;
    }

    @JsonProperty("status")
    public String getState() {
        return value;
    }

    public boolean isFinished() {
        return finished;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

