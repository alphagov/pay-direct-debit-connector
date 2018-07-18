package uk.gov.pay.directdebit.gatewayaccounts.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;

import java.util.Map;

public class GatewayAccountParser {
    GatewayAccount parse(Map<String, String> createGatewayAccountPayload) {
        GatewayAccount gatewayAccount = new GatewayAccount(
                PaymentProvider.fromString(createGatewayAccountPayload.get("payment_provider")),
                GatewayAccount.Type.fromString(createGatewayAccountPayload.get("type")),
                createGatewayAccountPayload.get("service_name"),
                createGatewayAccountPayload.get("description"),
                createGatewayAccountPayload.get("analytics_id")
        );

        String accessToken = createGatewayAccountPayload.get("access_token");
        if (accessToken != null) {
            gatewayAccount.setAccessToken(PaymentProviderAccessToken.of(accessToken));
        }
        String organisation = createGatewayAccountPayload.get("organisation");
        if (organisation != null) {
            gatewayAccount.setOrganisation(PaymentProviderOrganisationIdentifier.of(organisation));
        }

        return gatewayAccount;
    }
}
