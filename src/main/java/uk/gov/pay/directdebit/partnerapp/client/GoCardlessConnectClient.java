package uk.gov.pay.directdebit.partnerapp.client;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.GoCardlessConnectConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount.Type;
import uk.gov.pay.directdebit.partnerapp.client.model.GoCardlessConnectAccessTokenResponse;

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
 * This is required when linking a Partner app to a Merchant account
 */
public class GoCardlessConnectClient {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessConnectClient.class);
    private static final String CLIENT_SECRET_FIELD = "client_secret";
    private static final String CLIENT_ID_FIELD = "client_id";

    private final String testClientId;
    private final String testClientSecret;
    private final String testUrl;
    private final String liveClientId;
    private final String liveClientSecret;
    private final String liveUrl;

    private final Client client;

    @Inject
    public GoCardlessConnectClient(GoCardlessConnectConfig config, Client client) {
        this.client = client;
        this.liveClientId = config.getGoCardlessConnectClientIdLive();
        this.liveClientSecret = config.getGoCardlessConnectClientSecretLive();
        this.liveUrl = config.getGoCardlessConnectLiveUrl();
        this.testClientId = config.getGoCardlessConnectClientIdTest();
        this.testClientSecret = config.getGoCardlessConnectClientSecretTest();
        this.testUrl = config.getGoCardlessConnectTestUrl();
    }

    public Optional<GoCardlessConnectAccessTokenResponse> postAccessCode(String accessCode,
                                                                         GatewayAccount gatewayAccount,
                                                                         String redirectUri) {
        Form payload = new Form();
        payload.param("grant_type", "authorization_code");
        payload.param("code", accessCode);
        payload.param("redirect_uri", redirectUri);

        return (gatewayAccount.getType().equals(Type.TEST))
                ? targetTestAccount(payload)
                : targetLiveAccount(payload);
    }

    private Optional<GoCardlessConnectAccessTokenResponse> targetTestAccount(Form payload) {
        WebTarget webTarget = client.target(testUrl);
        payload.param(CLIENT_ID_FIELD, testClientId);
        payload.param(CLIENT_SECRET_FIELD, testClientSecret);

        return getAccessToken(webTarget, payload);
    }

    private Optional<GoCardlessConnectAccessTokenResponse> targetLiveAccount(Form payload) {
        WebTarget webTarget = client.target(liveUrl);
        payload.param(CLIENT_ID_FIELD, liveClientId);
        payload.param(CLIENT_SECRET_FIELD, liveClientSecret);

        return getAccessToken(webTarget, payload);
    }

    private Optional<GoCardlessConnectAccessTokenResponse> getAccessToken(WebTarget webTarget, Form payload) {
        try {
            Response response = webTarget.path("/oauth/access_token")
                    .request()
                    .post(Entity.entity(payload, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                            Response.class);
            GoCardlessConnectAccessTokenResponse entity = response.readEntity(GoCardlessConnectAccessTokenResponse.class);
            return Optional.of(entity);
        } catch (Exception exc) {
            LOGGER.error("Calling GoCardless Connect resulted in an error {}", exc.getMessage());
            return Optional.empty();
        }
    }

    public static boolean isValidResponse(GoCardlessConnectAccessTokenResponse response) {
        return StringUtils.isNotBlank(response.getAccessToken().toString()) && StringUtils.isNotBlank(response.getOrganisationId().toString());
    }
}
