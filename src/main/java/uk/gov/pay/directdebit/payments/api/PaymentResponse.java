package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import uk.gov.pay.commons.api.json.ApiResponseDateTimeSerializer;
import uk.gov.pay.directdebit.common.json.ToLowerCaseStringSerializer;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentId;

import java.time.ZonedDateTime;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;
import static uk.gov.pay.directdebit.payments.api.PaymentResponse.PaymentResponseBuilder.aPaymentResponse;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class PaymentResponse {
    
    @JsonProperty("payment_id")
    private String paymentExternalId;

    @JsonProperty
    private Long amount;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty("mandate_id")
    private MandateExternalId mandateId;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty("provider_id")
    private PaymentProviderPaymentId providerId;

    @JsonSerialize(using = ToLowerCaseStringSerializer.class)
    @JsonProperty("payment_provider")
    private PaymentProvider paymentProvider;

    @JsonProperty
    private String description;

    @JsonProperty
    private String reference;

    @JsonProperty("created_date")
    @JsonSerialize(using = ApiResponseDateTimeSerializer.class)
    private ZonedDateTime createdDate;

    @JsonProperty
    private ExternalPaymentStateWithDetails state;

    public PaymentResponse(PaymentResponseBuilder builder) {
        this.paymentExternalId = builder.paymentExternalId;
        this.state = builder.state;
        this.amount = builder.amount;
        this.mandateId = builder.mandateId;
        this.description = builder.description;
        this.providerId = builder.providerId;
        this.reference = builder.reference;
        this.createdDate = builder.createdDate;
        this.paymentProvider = builder.paymentProvider;
    }

    public PaymentProvider getPaymentProvider() {
        return paymentProvider;
    }


    public String getPaymentExternalId() {
        return paymentExternalId;
    }

    public Long getAmount() {
        return amount;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public ExternalPaymentStateWithDetails getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public static PaymentResponse from(Payment payment) {
        PaymentResponseBuilder paymentResponseBuilder = aPaymentResponse()
                .withPaymentExternalId(payment.getExternalId())
                .withState(new ExternalPaymentStateWithDetails(payment.getState().toExternal(), payment.getStateDetails().orElse(null)))
                .withAmount(payment.getAmount())
                .withMandateId(payment.getMandate().getExternalId())
                .withDescription(payment.getDescription())
                .withReference(payment.getReference())
                .withCreatedDate(payment.getCreatedDate())
                .withPaymentProvider(payment.getMandate().getGatewayAccount().getPaymentProvider());

        payment.getProviderId().ifPresent(paymentResponseBuilder::withProviderId);

        return paymentResponseBuilder.build();
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                " paymentExternalId='" + paymentExternalId + '\'' +
                ", amount=" + amount +
                ", mandateId='" + mandateId + '\'' +
                ", providerId='" + providerId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", description='" + description + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentResponse that = (PaymentResponse) o;
        return Objects.equals(paymentExternalId, that.paymentExternalId) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(mandateId, that.mandateId) &&
                Objects.equals(providerId, that.providerId) &&
                Objects.equals(paymentProvider, that.paymentProvider) &&
                Objects.equals(description, that.description) &&
                Objects.equals(reference, that.reference) &&
                Objects.equals(createdDate, that.createdDate) &&
                state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentExternalId, amount, mandateId, providerId, paymentProvider, description, reference, createdDate, state);
    }
    
    public static final class PaymentResponseBuilder {

        private String paymentExternalId;
        private Long amount;
        private MandateExternalId mandateId;
        private PaymentProvider paymentProvider;
        private String description;
        private String reference;
        private PaymentProviderPaymentId providerId;
        private ZonedDateTime createdDate;
        private ExternalPaymentStateWithDetails state;

        private PaymentResponseBuilder() {
        }

        public static PaymentResponseBuilder aPaymentResponse() {
            return new PaymentResponseBuilder();
        }
        
        public PaymentResponseBuilder withPaymentExternalId(String paymentExternalId) {
            this.paymentExternalId = paymentExternalId;
            return this;
        }

        public PaymentResponseBuilder withAmount(Long amount) {
            this.amount = amount;
            return this;
        }

        public PaymentResponseBuilder withMandateId(MandateExternalId mandateId) {
            this.mandateId = mandateId;
            return this;
        }

        public PaymentResponseBuilder withPaymentProvider(PaymentProvider paymentProvider) {
            this.paymentProvider = paymentProvider;
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
        
        public PaymentResponseBuilder withProviderId(PaymentProviderPaymentId providerId) {
            this.providerId = providerId;
            return this;
        }

        public PaymentResponse build() {
            return new PaymentResponse(this);
        }
    }
}


