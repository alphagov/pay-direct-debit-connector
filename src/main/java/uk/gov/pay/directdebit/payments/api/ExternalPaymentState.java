package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExternalPaymentState {
    EXTERNAL_CREATED("created", false),
    EXTERNAL_PENDING("pending", false),
    EXTERNAL_SUCCESS("success", true),
    EXTERNAL_FAILED("failed", true),
    EXTERNAL_CANCELLED("cancelled", true),
    EXTERNAL_PAID_OUT("paidout", true),
    EXTERNAL_INDEMNITY_CLAIM("indemnityclaim", true),
    EXTERNAL_ERROR("error", true);

    @JsonProperty("status")
    private final String status;
    
    private final boolean finished;
    private final String code;
    private final String message;

    ExternalPaymentState(String status, boolean finished) {
        this.status = status;
        this.finished = finished;
        this.code = null;
        this.message = null;
    }

    ExternalPaymentState(String status, boolean finished, String code, String message) {
        this.status = status;
        this.finished = finished;
        this.code = code;
        this.message = message;
    }
    
    public String getStatus() {
        return status;
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

    @Override
    public String toString() {
        return "ExternalPaymentState{" +
                "status='" + status + '\'' +
                ", finished=" + finished +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

