package uk.gov.pay.directdebit.gatewayaccounts.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;

import java.util.Map;

public class GatewayAccountParser {
    GatewayAccount parse(Map<String, String> createGatewayAccountPayload) {
        return new GatewayAccount(
                PaymentProvider.fromString(createGatewayAccountPayload.get("payment_provider")),
                GatewayAccount.Type.fromString(createGatewayAccountPayload.get("type")),
                createGatewayAccountPayload.get("service_name"),
                createGatewayAccountPayload.get("description"),
                createGatewayAccountPayload.get("analytics_id")
        );
    }
}
