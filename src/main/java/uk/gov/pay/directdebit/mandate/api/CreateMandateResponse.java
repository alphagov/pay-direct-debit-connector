package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.pay.directdebit.common.json.ToLowerCaseStringSerializer;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class CreateMandateResponse extends MandateResponse {

    @JsonProperty("description")
    private String description;
    
    @JsonProperty("payment_provider")
    @JsonSerialize(using = ToLowerCaseStringSerializer.class)
    private PaymentProvider paymentProvider;

    public CreateMandateResponse(MandateExternalId mandateId,
                                 String returnUrl,
                                 ZonedDateTime createdDate,
                                 ExternalMandateState state,
                                 List<Map<String, Object>> dataLinks,
                                 String serviceReference,
                                 MandateBankStatementReference mandateReference,
                                 String description,
                                 PaymentProvider paymentProvider) {
        super(mandateId, returnUrl, dataLinks, state, serviceReference, mandateReference, createdDate);
        this.description = description;
        this.paymentProvider = paymentProvider;
    }

    public MandateExternalId getMandateId() {
        return mandateId;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public ExternalMandateState getState() {
        return state;
    }

    public String getServiceReference() {
        return serviceReference;
    }

    public MandateBankStatementReference getMandateReference() {
        return mandateReference;
    }

    public URI getLink(String rel) {
        return dataLinks.stream()
                .filter(map -> rel.equals(map.get("rel")))
                .findFirst()
                .map(link -> (URI) link.get("href"))
                .get();
    }

}


