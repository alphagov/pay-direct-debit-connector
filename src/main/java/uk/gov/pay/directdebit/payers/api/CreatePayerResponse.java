package uk.gov.pay.directdebit.payers.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class CreatePayerResponse {

    @JsonProperty("payer_external_id")
    private String payerExternalId;

    public CreatePayerResponse(String payerExternalId) {
        this.payerExternalId = payerExternalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CreatePayerResponse that = (CreatePayerResponse) o;

        return payerExternalId != null ? payerExternalId.equals(that.payerExternalId) : that.payerExternalId == null;
    }

    @Override
    public int hashCode() {
        return payerExternalId != null ? payerExternalId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CreatePayerResponse{" +
                "payerExternalId='" + payerExternalId + '\'' +
                '}';
    }
}


