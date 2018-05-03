package uk.gov.pay.directdebit.notifications.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.notifications.model.EmailPayload;
import uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class EmailPayloadRequest {

    @JsonProperty
    private String address;

    @JsonProperty("gateway_account_external_id")
    private String gatewayAccountExternalId;

    @JsonProperty
    private EmailTemplate template;

    @JsonProperty
    private Map<String, String> personalisation;

    public static EmailPayloadRequest from(EmailPayload emailPayload) {
        return new EmailPayloadRequest(emailPayload.getAddress(), emailPayload.getGatewayAccountExternalId(),
                emailPayload.getTemplate(), emailPayload.getPersonalisation());
    }

    public EmailPayloadRequest(String address, String gatewayAccountExternalId, EmailTemplate template,
                               Map<String, String> personalisation) {
        this.address = address;
        this.gatewayAccountExternalId = gatewayAccountExternalId;
        this.template = template;
        this.personalisation = personalisation;
    }
}
