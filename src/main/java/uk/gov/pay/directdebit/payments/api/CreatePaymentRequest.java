package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.mandate.api.CreateRequest;
import uk.gov.pay.directdebit.mandate.model.MandateType;

import java.util.Map;

public class CreatePaymentRequest implements CreateRequest, CollectRequest {
    @JsonProperty("return_url")
    private String returnUrl;
    
    @JsonProperty("amount")
    private Long amount;

    @JsonProperty("description")
    private String description;

    @JsonProperty("reference")
    private String reference;

    private CreatePaymentRequest(String returnUrl, Long amount, String description,
            String reference) {
        this.returnUrl = returnUrl;
        this.amount = amount;
        this.description = description;
        this.reference = reference;
    }

    public static CreatePaymentRequest of(Map<String, String> createPaymentRequest) {
        return new CreatePaymentRequest(
                createPaymentRequest.get("return_url"),
                Long.valueOf(createPaymentRequest.get("amount")),
                createPaymentRequest.get("description"),
                createPaymentRequest.get("reference")
        );
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    @Override
    public MandateType getMandateType() {
        return MandateType.ONE_OFF;
    }

    public Long getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }
}
