package uk.gov.pay.directdebit.notifications.clients;

import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.AdminUsersConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.api.EmailPayloadRequest;
import uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;

public class AdminUsersClient {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(AdminUsersClient.class);


    private final Client client;
    private final AdminUsersConfig config;

    public AdminUsersClient(AdminUsersConfig config, Client client) {
        this.client = client;
        this.config = config;
    }

    public void sendEmail(EmailTemplate template, Mandate mandate, Map<String, String> personalisation) {
        LOGGER.info("Calling adminusers to send {} email for mandate id {} for gateway account id {}",
                template.toString(),
                mandate.getExternalId(),
                mandate.getGatewayAccount().getExternalId());
        EmailPayloadRequest emailPayloadRequest = new EmailPayloadRequest(mandate.getPayer().getEmail(), mandate.getGatewayAccount().getExternalId(), template, personalisation);
        try {
             Response response = client.target(config.getAdminUsersUrl())
                    .path("/v1/emails/send")
                    .request()
                    .post(Entity.entity(emailPayloadRequest, MediaType.APPLICATION_JSON_TYPE));
            response.readEntity(String.class);
        } catch (Exception exc) {
            LOGGER.error("Making call to adminusers failed with exception {}", exc.getMessage());
        }
    }
}
