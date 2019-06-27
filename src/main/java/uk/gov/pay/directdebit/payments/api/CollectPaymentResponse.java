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
import static uk.gov.pay.directdebit.payments.api.CollectPaymentResponse.CollectPaymentResponseBuilder.aCollectPaymentResponse;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class CollectPaymentResponse {
    @JsonProperty("links")
    private List<Map<String, Object>> dataLinks;
    
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

    public CollectPaymentResponse(CollectPaymentResponseBuilder builder) {
        this.paymentExternalId = builder.paymentExternalId;
        this.state = builder.state;
        this.dataLinks = builder.dataLinks;
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

    public List<Map<String, Object>> getDataLinks() {
        return dataLinks;
    }

    public String getPaymentExternalId() {
        return paymentExternalId;
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

    public URI getLink(String rel) {
        return dataLinks.stream()
                .filter(map -> rel.equals(map.get("rel")))
                .findFirst()
                .map(link -> (URI) link.get("href"))
                .get();
    }

    public static CollectPaymentResponse from(Payment payment, List<Map<String, Object>> dataLinks) {
        CollectPaymentResponseBuilder collectPaymentResponseBuilder = aCollectPaymentResponse()
                .withPaymentExternalId(payment.getExternalId())
                // TODO: should extract state details (go cardless cause details) from events table somehow
                .withState(new ExternalPaymentStateWithDetails(payment.getState().toExternal(), "example_details"))
                .withAmount(payment.getAmount())
                .withMandateId(payment.getMandate().getExternalId())
                .withDescription(payment.getDescription())
                .withReference(payment.getReference())
                .withCreatedDate(payment.getCreatedDate())
                .withPaymentProvider(payment.getMandate().getGatewayAccount().getPaymentProvider())
                .withDataLinks(dataLinks);

        payment.getProviderId().ifPresent(collectPaymentResponseBuilder::withProviderId);

        return collectPaymentResponseBuilder.build();
    }

    @Override
    public String toString() {
        return "CollectPaymentResponse{" +
                "dataLinks=" + dataLinks +
                ", paymentExternalId='" + paymentExternalId + '\'' +
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
        CollectPaymentResponse that = (CollectPaymentResponse) o;
        return Objects.equals(dataLinks, that.dataLinks) &&
                Objects.equals(paymentExternalId, that.paymentExternalId) &&
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
        return Objects.hash(dataLinks, paymentExternalId, amount, mandateId, providerId, paymentProvider, description, reference, createdDate, state);
    }

    public static final class CollectPaymentResponseBuilder {

        private List<Map<String, Object>> dataLinks;
        private String paymentExternalId;
        private Long amount;
        private MandateExternalId mandateId;
        private PaymentProvider paymentProvider;
        private String description;
        private String reference;
        private PaymentProviderPaymentId providerId;
        private ZonedDateTime createdDate;
        private ExternalPaymentStateWithDetails state;

        private CollectPaymentResponseBuilder() {
        }

        public static CollectPaymentResponseBuilder aCollectPaymentResponse() {
            return new CollectPaymentResponseBuilder();
        }

        public CollectPaymentResponseBuilder withDataLinks(List<Map<String, Object>> dataLinks) {
            this.dataLinks = dataLinks;
            return this;
        }

        public CollectPaymentResponseBuilder withPaymentExternalId(String paymentExternalId) {
            this.paymentExternalId = paymentExternalId;
            return this;
        }

        public CollectPaymentResponseBuilder withAmount(Long amount) {
            this.amount = amount;
            return this;
        }

        public CollectPaymentResponseBuilder withMandateId(MandateExternalId mandateId) {
            this.mandateId = mandateId;
            return this;
        }

        public CollectPaymentResponseBuilder withPaymentProvider(PaymentProvider paymentProvider) {
            this.paymentProvider = paymentProvider;
            return this;
        }

        public CollectPaymentResponseBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public CollectPaymentResponseBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public CollectPaymentResponseBuilder withCreatedDate(ZonedDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public CollectPaymentResponseBuilder withState(ExternalPaymentStateWithDetails state) {
            this.state = state;
            return this;
        }
        
        public CollectPaymentResponseBuilder withProviderId(PaymentProviderPaymentId providerId) {
            this.providerId = providerId;
            return this;
        }

        public CollectPaymentResponse build() {
            return new CollectPaymentResponse(this);
        }
    }
}


