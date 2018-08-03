package uk.gov.pay.directdebit.partnerapp.client;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.GoCardlessConnectConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;
import uk.gov.pay.directdebit.partnerapp.client.model.GoCardlessConnectClientResponse;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;


/**
 * An Http client used to perform OAuth requests to GoCardless
 */
public class GoCardlessConnectClient {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessConnectClient.class);
    private final GoCardlessConnectConfig config;

    private Client client;

    @Inject
    public GoCardlessConnectClient(GoCardlessConnectConfig config, Client client) {
        this.config = config;
        this.client = client;
    }

    public Optional<GoCardlessConnectClientResponse> postAccessCode(String accessCode,
                                                                    GatewayAccount gatewayAccount,
                                                                    String redirectUri) {
        WebTarget webTarget = getWebTarget(gatewayAccount);
        Form payload = createPostPayload(accessCode, gatewayAccount, redirectUri);

        try {
            Response response = webTarget.path("/oauth/access_token")
                    .request()
                    .post(Entity.entity(payload, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                            Response.class);
            GoCardlessConnectClientResponse entity = response.readEntity(GoCardlessConnectClientResponse.class);
            return Optional.of(entity);
        } catch (Exception exc) {
            throw new BadRequestException("Error calling GoCardless Connect client");
        }
    }

    private Form createPostPayload(String accessCode,
                                                             GatewayAccount gatewayAccount,
                                                             String redirectUri) {
        Form payload = new Form();
        payload.param("grant_type", "authorization_code");
        payload.param("code", accessCode);
        payload.param("redirect_uri", redirectUri);
        payload.param("client_id", getClientId(gatewayAccount));
        payload.param("client_secret", getClientSecret(gatewayAccount));

        return payload;
    }

    private String getClientId(GatewayAccount gatewayAccount) {
        if (gatewayAccount.getType().equals(GatewayAccount.Type.TEST)) {
            return config.getGoCardlessConnectClientIdTest();
        } else {
            return config.getGoCardlessConnectClientIdLive();
        }
    }

    private String getClientSecret(GatewayAccount gatewayAccount) {
        if (gatewayAccount.getType().equals(GatewayAccount.Type.TEST)) {
            return config.getGoCardlessConnectClientSecretTest();
        } else {
            return config.getGoCardlessConnectClientSecretLive();
        }
    }

    private WebTarget getWebTarget(GatewayAccount gatewayAccount) {
        if (gatewayAccount.getType().equals(GatewayAccount.Type.TEST)) {
            return client.target(config.getGoCardlessConnectTestUrl());
        } else {
            return client.target(config.getGoCardlessConnectLiveUrl());
        }
    }

    public static boolean isValidResponse(GoCardlessConnectClientResponse response) {
        return isValidAccessToken(response.getAccessToken()) && isValidOrganisationIdentifier(response.getOrganisationId());
    }

    private static boolean isValidAccessToken(PaymentProviderAccessToken accessToken) {
        return accessToken != null && StringUtils.isNotBlank(accessToken.toString());
    }

    private static boolean isValidOrganisationIdentifier(PaymentProviderOrganisationIdentifier organisationIdentifier) {
        return organisationIdentifier != null && StringUtils.isNotBlank(organisationIdentifier.toString());
    }
}
