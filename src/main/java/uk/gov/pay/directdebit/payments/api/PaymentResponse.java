package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.pay.commons.api.json.ApiResponseDateTimeSerializer;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payments.model.Payment;

import java.time.ZonedDateTime;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;
import static uk.gov.pay.directdebit.payments.api.PaymentResponse.PaymentResponseBuilder.aPaymentResponse;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class PaymentResponse {
    
    //compatibility with public api
    @JsonProperty("charge_id")
    private String transactionExternalId;

    @JsonProperty
    private Long amount;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty
    private String description;
    
    @JsonProperty("mandate_id")
    private MandateExternalId mandateId;
    
    @JsonProperty
    private String reference;

    @JsonProperty("created_date")
    @JsonSerialize(using = ApiResponseDateTimeSerializer.class)
    private ZonedDateTime createdDate;

    @JsonProperty
    private ExternalPaymentStateWithDetails state;

    PaymentResponse(PaymentResponseBuilder builder) {
        this.transactionExternalId = builder.transactionExternalId;
        this.state = builder.state;
        this.amount = builder.amount;
        this.description = builder.description;
        this.reference = builder.reference;
        this.createdDate = builder.createdDate;
        this.mandateId = builder.mandateId;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public String getTransactionExternalId() {
        return transactionExternalId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getReturnUrl() {
        return returnUrl;
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


    public static PaymentResponse from(Payment payment) {
        return aPaymentResponse()
                .withCreatedDate(payment.getCreatedDate())
                .withReference(payment.getReference())
                .withState(new ExternalPaymentStateWithDetails(payment.getState().toExternal(),
                        "example_details"))
                .withAmount(payment.getAmount())
                .withTransactionExternalId(payment.getExternalId())
                .withDescription(payment.getDescription())
                .withMandateId(payment.getMandate().getExternalId())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentResponse that = (PaymentResponse) o;

        if (!transactionExternalId.equals(that.transactionExternalId)) return false;
        if (!amount.equals(that.amount)) return false;
        if (!returnUrl.equals(that.returnUrl)) return false;
        if (!Objects.equals(description, that.description)) return false;
        if (!Objects.equals(reference, that.reference)) return false;
        if (!createdDate.equals(that.createdDate)) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = 31 * transactionExternalId.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        result = 31 * result + createdDate.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                " paymentExternalId='" + transactionExternalId + '\'' +
                ", state='" + state + '\'' +
                ", amount=" + amount +
                ", returnUrl='" + returnUrl + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }

    public static final class PaymentResponseBuilder {
        //compatibility with public api
        private String transactionExternalId;
        private Long amount;
        private String description;
        private String reference;
        private ZonedDateTime createdDate;
        private ExternalPaymentStateWithDetails state;
        private MandateExternalId mandateId;

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

        public PaymentResponseBuilder withMandateId(MandateExternalId mandateId) {
            this.mandateId = mandateId;
            return this;
        }
        
        public PaymentResponse build() {
            return new PaymentResponse(this);
        }
    }
}


