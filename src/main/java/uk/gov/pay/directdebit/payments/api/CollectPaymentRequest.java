package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import java.util.Map;

public class CollectPaymentRequest implements CollectRequest {
    @JsonProperty("mandate_id")
    private MandateExternalId mandateExternalId;

    @JsonProperty("amount")
    private Long amount;

    @JsonProperty("description")
    private String description;

    @JsonProperty("reference")
    private String reference;

    public CollectPaymentRequest(MandateExternalId mandateExternalId, Long amount, String description,
                                 String reference) {
        this.mandateExternalId = mandateExternalId;
        this.amount = amount;
        this.description = description;
        this.reference = reference;
    }

    public static CollectPaymentRequest of(Map<String, String> collectPaymentRequest) {
        return new CollectPaymentRequest(
                MandateExternalId.valueOf(collectPaymentRequest.get("mandate_id")),
                Long.valueOf(collectPaymentRequest.get("amount")),
                collectPaymentRequest.get("description"),
                collectPaymentRequest.get("reference")
        );
    }

    public MandateExternalId getMandateExternalId() {
        return mandateExternalId;
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
