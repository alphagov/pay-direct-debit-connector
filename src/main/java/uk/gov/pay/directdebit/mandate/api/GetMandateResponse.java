package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class GetMandateResponse {
    @JsonProperty("mandate_id")
    private String mandateId;

    @JsonProperty("mandate_type")
    private MandateType mandateType;
    
    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("links")
    private List<Map<String, Object>> dataLinks;
    
    @JsonProperty
    private ExternalMandateState state;

    public GetMandateResponse(String mandateId,
            MandateType mandateType, String returnUrl,
            List<Map<String, Object>> dataLinks,
            ExternalMandateState state) {
        this.mandateId = mandateId;
        this.mandateType = mandateType;
        this.returnUrl = returnUrl;
        this.dataLinks = dataLinks;
        this.state = state;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getMandateId() {
        return mandateId;
    }

    public MandateType getMandateType() {
        return mandateType;
    }

    public List<Map<String, Object>> getDataLinks() {
        return dataLinks;
    }

    public ExternalMandateState getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GetMandateResponse that = (GetMandateResponse) o;

        if (!mandateId.equals(that.mandateId)) {
            return false;
        }
        if (mandateType != that.mandateType) {
            return false;
        }
        if (!returnUrl.equals(that.returnUrl)) {
            return false;
        }
        if (!dataLinks.equals(that.dataLinks)) {
            return false;
        }
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = mandateId.hashCode();
        result = 31 * result + mandateType.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + dataLinks.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }
}


