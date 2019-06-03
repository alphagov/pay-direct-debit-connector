package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import uk.gov.pay.commons.api.json.ApiResponseDateTimeSerializer;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class CreateMandateResponse {
    @JsonProperty("links")
    private List<Map<String, Object>> dataLinks;

    @JsonProperty("mandate_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private MandateExternalId mandateId;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("created_date")
    @JsonSerialize(using = ApiResponseDateTimeSerializer.class)
    private ZonedDateTime createdDate;

    @JsonProperty
    private ExternalMandateState state;

    @JsonProperty("service_reference")
    private String serviceReference;

    @JsonProperty("mandate_reference")
    @JsonSerialize(using = ToStringSerializer.class)
    private MandateBankStatementReference mandateReference;

    public CreateMandateResponse(MandateExternalId mandateId,
                                 String returnUrl,
                                 ZonedDateTime createdDate,
                                 ExternalMandateState state,
                                 List<Map<String, Object>> dataLinks,
                                 String serviceReference,
                                 MandateBankStatementReference mandateReference) {
        this.dataLinks = dataLinks;
        this.mandateId = mandateId;
        this.returnUrl = returnUrl;
        this.createdDate = createdDate;
        this.state = state;
        this.serviceReference = serviceReference;
        this.mandateReference = mandateReference;
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


