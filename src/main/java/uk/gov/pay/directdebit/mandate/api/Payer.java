package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Payer {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("email")
    private final String email;

    private Payer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public static Payer from(uk.gov.pay.directdebit.payers.model.Payer payer) {
        return new Payer(payer.getName(), payer.getEmail());
    }
}
