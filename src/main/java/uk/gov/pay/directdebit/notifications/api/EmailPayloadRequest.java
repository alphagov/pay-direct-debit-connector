package uk.gov.pay.directdebit.notifications.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.notifications.model.EmailPayload;
import uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class EmailPayloadRequest {

    @JsonProperty
    private String address;

    @JsonProperty("gateway_account_id")
    private String gatewayAccountId;

    @JsonProperty
    private EmailTemplate template;

    @JsonProperty
    private Map<String, String> personalisation;

    public static EmailPayloadRequest from(EmailPayload emailPayload) {
        return new EmailPayloadRequest(emailPayload.getAddress(), emailPayload.getGatewayAccountId(),
                emailPayload.getTemplate(), emailPayload.getPersonalisation());
    }

    public EmailPayloadRequest(String address, String gatewayAccountId, EmailTemplate template,
                                Map<String, String> personalisation) {
        this.address = address;
        this.gatewayAccountId = gatewayAccountId;
        this.template = template;
        this.personalisation = personalisation;
    }
}
