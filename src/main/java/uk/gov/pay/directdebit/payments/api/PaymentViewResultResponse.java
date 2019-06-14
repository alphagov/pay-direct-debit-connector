package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.pay.commons.api.json.ApiResponseDateTimeSerializer;
import uk.gov.pay.directdebit.payments.links.Link;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class PaymentViewResultResponse {

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty
    private Long amount;

    @JsonProperty
    private String description;

    @JsonProperty
    private String reference;

    @JsonProperty("created_date")
    @JsonSerialize(using = ApiResponseDateTimeSerializer.class)
    private ZonedDateTime createdDate;

    @JsonProperty("agreement_id")
    private String mandateExternalId;

    @JsonProperty
    private String name;

    @JsonProperty
    private String email;

    @JsonProperty
    private ExternalPaymentState state;

    @JsonProperty
    private List<Link> links = new ArrayList<>();


    public PaymentViewResultResponse(String paymentId,
                                     Long amount,
                                     String reference,
                                     String description,
                                     ZonedDateTime createdDate,
                                     String name,
                                     String email,
                                     ExternalPaymentState state,
                                     String mandateExternalId) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.createdDate = createdDate;
        this.name = name;
        this.email = email;
        this.state = state;
        this.mandateExternalId = mandateExternalId;
    }

    public String getPaymentId() {
        return paymentId;
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

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public ExternalPaymentState getState() {
        return state;
    }

    public List<Link> getLinks() {
        return links;
    }

    public PaymentViewResultResponse withLink(Link link) {
        this.links.add(link);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentViewResultResponse that = (PaymentViewResultResponse) o;

        return Objects.equals(paymentId, that.paymentId)
                && Objects.equals(amount, that.amount)
                && Objects.equals(description, that.description)
                && Objects.equals(reference, that.reference)
                && Objects.equals(createdDate, that.createdDate)
                && Objects.equals(name, that.name)
                && (email == null ? that.email == null : email.equalsIgnoreCase(that.email))
                && (mandateExternalId == null ? that.mandateExternalId == null : mandateExternalId.equalsIgnoreCase(that.mandateExternalId))
                && Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, amount, description, reference, createdDate, name, email, state, mandateExternalId);
    }

    @Override
    public String toString() {
        return format("PaymentResponse{paymentId='%s', amount='%s', reference='%s', createdDate='%s', name='%s', email='%s', state='%s'}",
                paymentId, amount, reference, createdDate, name, email, state.getState());
    }
}
