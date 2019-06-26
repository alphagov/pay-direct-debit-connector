package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.pay.commons.api.json.ApiResponseDateTimeSerializer;
import uk.gov.pay.directdebit.payments.model.Payment;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

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

    public PaymentResponse(String transactionExternalId, ExternalPaymentStateWithDetails state, Long amount, String returnUrl, String description, String reference, ZonedDateTime createdDate, List<Map<String, Object>> dataLinks) {
        this.transactionExternalId = transactionExternalId;
        this.state = state;
        this.dataLinks = dataLinks;
        this.amount = amount;
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.createdDate = createdDate;
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


    public static PaymentResponse from(Payment payment, List<Map<String, Object>> dataLinks) {
        return new PaymentResponse(
                payment.getExternalId(),
                // TODO: should extract state details (go cardless cause details) from events table somehow
                new ExternalPaymentStateWithDetails(payment.getState().toExternal(), "example_details"),
                payment.getAmount(),
                payment.getMandate().getReturnUrl(),
                payment.getDescription(),
                payment.getReference(),
                payment.getCreatedDate(),
                dataLinks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentResponse that = (PaymentResponse) o;

        if (!Objects.equals(dataLinks, that.dataLinks)) return false;
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
        int result = dataLinks != null ? dataLinks.hashCode() : 0;
        result = 31 * result + transactionExternalId.hashCode();
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
                "dataLinks=" + dataLinks +
                ", paymentExternalId='" + transactionExternalId + '\'' +
                ", state='" + state + '\'' +
                ", amount=" + amount +
                ", returnUrl='" + returnUrl + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }

    public static final class PaymentResponseBuilder {
        private List<Map<String, Object>> dataLinks;
        //compatibility with public api
        private String transactionExternalId;
        private Long amount;
        private String returnUrl;
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

        public PaymentResponse build() {
            return new PaymentResponse(transactionExternalId, state, amount, returnUrl, description, reference, createdDate, dataLinks);
        }
    }
}


