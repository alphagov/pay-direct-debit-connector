package uk.gov.pay.directdebit.notifications.clients;

import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.AdminUsersConfig;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.exception.PayerNotFoundException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.api.EmailPayloadRequest;
import uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;

import static java.lang.String.format;

public class AdminUsersClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUsersClient.class);

    private final Client client;
    private final AdminUsersConfig config;

    public AdminUsersClient(AdminUsersConfig config, Client client) {
        this.client = client;
        this.config = config;
    }

    public void sendEmail(EmailTemplate template, Mandate mandate, Map<String, String> personalisation) {
        String gatewayAccountExternalId = mandate.getGatewayAccount().getExternalId();
        LOGGER.info("Calling adminusers to send {} email for mandate id {} for gateway account id {}",
                template.toString(),
                mandate.getExternalId(),
                gatewayAccountExternalId);
        String email = mandate.getPayer().orElseThrow(() -> new PayerNotFoundException(mandate.getExternalId())).getEmail();
        var emailPayloadRequest = new EmailPayloadRequest(email, gatewayAccountExternalId, template, personalisation);
        try {
            Response response = client.target(config.getAdminUsersUrl())
                    .path("/v1/emails/send")
                    .request()
                    .post(Entity.entity(emailPayloadRequest, MediaType.APPLICATION_JSON_TYPE));
            if (response.getStatus() != 200) {
                throw new RuntimeException(format("Sending email failed with status %s and response %s", response.getStatus(), response.readEntity(String.class)));
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to send {} email for mandate id {} for gateway account id {}",
                    template.toString(),
                    mandate.getExternalId(),
                    gatewayAccountExternalId,
                    exception);
            throw exception;
        }
    }
}
