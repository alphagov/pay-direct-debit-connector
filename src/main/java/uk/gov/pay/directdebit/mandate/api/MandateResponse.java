package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import uk.gov.pay.directdebit.common.json.ToLowerCaseStringSerializer;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateId;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.service.payments.commons.api.json.ApiResponseDateTimeSerializer;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MandateResponse {

    @JsonProperty("mandate_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private final MandateExternalId mandateId;

    @JsonProperty("return_url")
    private final String returnUrl;

    @JsonProperty("links")
    private final List<Map<String, Object>> dataLinks;

    @JsonProperty
    private ExternalMandateStateWithDetails state;

    @JsonProperty("service_reference")
    private final String serviceReference;

    @JsonProperty("mandate_reference")
    @JsonSerialize(using = ToStringSerializer.class)
    private final MandateBankStatementReference mandateReference;

    @JsonProperty("created_date")
    @JsonSerialize(using = ApiResponseDateTimeSerializer.class)
    private final ZonedDateTime createdDate;

    @JsonProperty("provider_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private final PaymentProviderMandateId paymentProviderId;

    @JsonProperty("payment_provider")
    @JsonSerialize(using = ToLowerCaseStringSerializer.class)
    private final PaymentProvider paymentProvider;

    @JsonProperty("description")
    private final String description;
    
    @JsonProperty
    private final Payer payer;

    public MandateResponse(Mandate mandate, List<Map<String, Object>> dataLinks) {
        mandateId = mandate.getExternalId();
        returnUrl = mandate.getReturnUrl();
        this.dataLinks = dataLinks;
        state = new ExternalMandateStateWithDetails(mandate.getState().toExternal(), mandate.getStateDetails().orElse(null));
        serviceReference = mandate.getServiceReference();
        mandateReference = mandate.getMandateBankStatementReference().orElse(null);
        paymentProviderId = mandate.getPaymentProviderMandateId().orElse(null);
        createdDate = mandate.getCreatedDate();
        paymentProvider = mandate.getGatewayAccount().getPaymentProvider();
        description = mandate.getDescription().orElse(null);
        payer = mandate.getPayer().map(Payer::from).orElse(null);
    }

    public Optional<URI> getLink(String rel) {
        return dataLinks.stream()
                .filter(map -> rel.equals(map.get("rel")))
                .findFirst()
                .map(link -> (URI) link.get("href"));
    }

    public MandateExternalId getMandateId() {
        return mandateId;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public List<Map<String, Object>> getDataLinks() {
        return dataLinks;
    }

    public ExternalMandateStateWithDetails getState() {
        return state;
    }

    public String getServiceReference() {
        return serviceReference;
    }

    public MandateBankStatementReference getMandateReference() {
        return mandateReference;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public PaymentProviderMandateId getPaymentProviderId() {
        return paymentProviderId;
    }
}
