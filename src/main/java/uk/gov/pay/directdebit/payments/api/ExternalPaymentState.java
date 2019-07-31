package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExternalPaymentState {
    EXTERNAL_CREATED("created"),
    EXTERNAL_PENDING("pending"),
    EXTERNAL_SUCCESS("success"),
    EXTERNAL_FAILED("failed"),
    EXTERNAL_CANCELLED("cancelled"),
    EXTERNAL_PAID_OUT("paidout"),
    EXTERNAL_INDEMNITY_CLAIM("indemnityclaim"),
    EXTERNAL_ERROR("error");

    @JsonProperty("status")
    private final String status;

    ExternalPaymentState(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ExternalPaymentState{" +
                "status='" + status + '\'' +
                '}';
    }
}

