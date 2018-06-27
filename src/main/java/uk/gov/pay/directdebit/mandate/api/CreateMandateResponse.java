package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.mandate.model.MandateType;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class CreateMandateResponse {
    @JsonProperty("links")
    private List<Map<String, Object>> dataLinks;

    @JsonProperty("mandate_id")
    private String mandateId;

    @JsonProperty("mandate_type")
    private MandateType mandateType;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty
    private ExternalMandateState state;

    @JsonProperty("service_reference")
    private String serviceReference;

    @JsonProperty("mandate_reference")
    private String mandateReference;

    public CreateMandateResponse(String mandateId,
                                 MandateType mandateType,
                                 String returnUrl,
                                 String createdDate,
                                 ExternalMandateState state,
                                 List<Map<String, Object>> dataLinks,
                                 String serviceReference,
                                 String mandateReference) {
        this.dataLinks = dataLinks;
        this.mandateId = mandateId;
        this.mandateType = mandateType;
        this.returnUrl = returnUrl;
        this.createdDate = createdDate;
        this.state = state;
        this.serviceReference = serviceReference;
        this.mandateReference = mandateReference;
    }

    public String getMandateId() {
        return mandateId;
    }

    public MandateType getMandateType() {
        return mandateType;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public ExternalMandateState getState() {
        return state;
    }

    public String getServiceReference() {
        return serviceReference;
    }

    public String getMandateReference() {
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


