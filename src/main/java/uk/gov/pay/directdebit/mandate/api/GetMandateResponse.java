package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateId;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class GetMandateResponse extends MandateResponse {

    @JsonProperty("provider_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private final PaymentProviderMandateId paymentProviderId;
    
    public GetMandateResponse(Mandate mandate, List<Map<String, Object>> dataLinks) {
        super(mandate.getExternalId(), mandate.getReturnUrl(), dataLinks, mandate.getState().toExternal(),
                mandate.getServiceReference(), mandate.getMandateBankStatementReference(), mandate.getCreatedDate());
        this.paymentProviderId = mandate.getPaymentProviderMandateId().orElse(null);
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public MandateExternalId getMandateId() {
        return mandateId;
    }
    
    public List<Map<String, Object>> getDataLinks() {
        return dataLinks;
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
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + dataLinks.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

}


