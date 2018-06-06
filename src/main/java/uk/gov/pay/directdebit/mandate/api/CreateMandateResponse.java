package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import java.util.Map;
import uk.gov.pay.directdebit.mandate.model.MandateType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class CreateMandateResponse {
    @JsonProperty("links")
    private List<Map<String, Object>> dataLinks;

    @JsonProperty("agreement_id")
    private String agreementId;

    @JsonProperty("agreement_type")
    private MandateType agreementType;
    
    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty
    private ExternalMandateState state;

    public CreateMandateResponse(String agreementId,
            MandateType agreementType, String returnUrl, String createdDate,
            ExternalMandateState state,
            List<Map<String, Object>> dataLinks) {
        this.dataLinks = dataLinks;
        this.agreementId = agreementId;
        this.agreementType = agreementType;
        this.returnUrl = returnUrl;
        this.createdDate = createdDate;
        this.state = state;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public MandateType getAgreementType() {
        return agreementType;
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

    public URI getLink(String rel) {
        return dataLinks.stream()
                .filter(map -> rel.equals(map.get("rel")))
                .findFirst()
                .map(link -> (URI) link.get("href"))
                .get();
    }

}


