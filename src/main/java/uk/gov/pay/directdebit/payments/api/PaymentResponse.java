package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.pay.commons.api.json.ApiResponseDateTimeSerializer;
import uk.gov.pay.directdebit.payments.model.Payment;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;
import static uk.gov.pay.directdebit.payments.api.PaymentResponse.PaymentResponseBuilder.aPaymentResponse;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class PaymentResponse {
    @JsonProperty("links")
    private List<Map<String, Object>> dataLinks;

    //compatibility with public api
    @JsonProperty("charge_id")
    private String transactionExternalId;

    @JsonProperty
    private Long amount;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty
    private String description;

    @JsonProperty
    private String reference;

    @JsonProperty("created_date")
    @JsonSerialize(using = ApiResponseDateTimeSerializer.class)
    private ZonedDateTime createdDate;

    @JsonProperty
    private ExternalPaymentStateWithDetails state;

    private PaymentResponse(PaymentResponseBuilder builder) {
        this.transactionExternalId = builder.transactionExternalId;
        this.state = builder.state;
        this.dataLinks = builder.dataLinks;
        this.amount = builder.amount;
        this.description = builder.description;
        this.reference = builder.reference;
        this.createdDate = builder.createdDate;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public String getTransactionExternalId() {
        return transactionExternalId;
    }

    public String getReturnUrl() {
        return returnUrl;
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

    public ExternalPaymentStateWithDetails getState() {
        return state;
    }


    public static PaymentResponse from(Payment payment, List<Map<String, Object>> dataLinks) {
        // TODO: should extract state details (go cardless cause details) from events table somehow
        return aPaymentResponse()
                .withTransactionExternalId(payment.getExternalId())
                .withState(
                        new ExternalPaymentStateWithDetails(payment.getState().toExternal(), "example_details"))
                .withAmount(payment.getAmount())
                .withReference(payment.getReference())
                .withDescription(payment.getDescription())
                .withCreatedDate(payment.getCreatedDate())
                .withDataLinks(dataLinks)
                .build();
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "dataLinks=" + dataLinks +
                ", transactionExternalId='" + transactionExternalId + '\'' +
                ", amount=" + amount +
                ", returnUrl='" + returnUrl + '\'' +
                ", description='" + description + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate=" + createdDate +
                ", state=" + state +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentResponse that = (PaymentResponse) o;
        return Objects.equals(dataLinks, that.dataLinks) &&
                Objects.equals(transactionExternalId, that.transactionExternalId) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(returnUrl, that.returnUrl) &&
                Objects.equals(description, that.description) &&
                Objects.equals(reference, that.reference) &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataLinks, transactionExternalId, amount, returnUrl, description, reference, createdDate, state);
    }

    public static final class PaymentResponseBuilder {
        private List<Map<String, Object>> dataLinks;
        //compatibility with public api
        private String transactionExternalId;
        private Long amount;
        private String description;
        private String reference;
        private ZonedDateTime createdDate;
        private ExternalPaymentStateWithDetails state;

        private PaymentResponseBuilder() {
        }

        public static PaymentResponseBuilder aPaymentResponse() {
            return new PaymentResponseBuilder();
        }

        public PaymentResponseBuilder withTransactionExternalId(String transactionExternalId) {
            this.transactionExternalId = transactionExternalId;
            return this;
        }

        public PaymentResponseBuilder withAmount(Long amount) {
            this.amount = amount;
            return this;
        }

        public PaymentResponseBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public PaymentResponseBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public PaymentResponseBuilder withCreatedDate(ZonedDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public PaymentResponseBuilder withState(ExternalPaymentStateWithDetails state) {
            this.state = state;
            return this;
        }
        
        public PaymentResponseBuilder withDataLinks(List<Map<String, Object>> dataLinks) {
            this.dataLinks = dataLinks;
            return this;
        }

        public PaymentResponse build() {
            return new PaymentResponse(this);
        }
    }
}


