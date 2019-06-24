package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import uk.gov.pay.commons.api.json.ApiResponseDateTimeSerializer;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

abstract class MandateResponse {

    @JsonProperty("mandate_id")
    @JsonSerialize(using = ToStringSerializer.class)
    protected final MandateExternalId mandateId;

    @JsonProperty("return_url")
    protected final String returnUrl;

    @JsonProperty("links")
    protected final List<Map<String, Object>> dataLinks;

    @JsonProperty
    protected final ExternalMandateState state;

    @JsonProperty("service_reference")
    protected final String serviceReference;

    @JsonProperty("mandate_reference")
    @JsonSerialize(using = ToStringSerializer.class)
    protected final MandateBankStatementReference mandateReference;

    @JsonProperty("created_date")
    @JsonSerialize(using = ApiResponseDateTimeSerializer.class)
    protected final ZonedDateTime createdDate;

    protected MandateResponse(MandateExternalId mandateId, 
                              String returnUrl, 
                              List<Map<String, Object>> dataLinks, 
                              ExternalMandateState state, 
                              String serviceReference, 
                              MandateBankStatementReference mandateReference, 
                              ZonedDateTime createdDate) {
        this.mandateId = mandateId;
        this.returnUrl = returnUrl;
        this.dataLinks = dataLinks;
        this.state = state;
        this.serviceReference = serviceReference;
        this.mandateReference = mandateReference;
        this.createdDate = createdDate;
    }
}
