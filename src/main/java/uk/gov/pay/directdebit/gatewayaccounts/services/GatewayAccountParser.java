package uk.gov.pay.directdebit.gatewayaccounts.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;

import java.util.Map;
import java.util.Optional;

public class GatewayAccountParser {
    //todo should type and payment provider be optional?
    GatewayAccount parse(Map<String, String> createGatewayAccountPayload) {
        GatewayAccount.Type type = Optional.ofNullable(createGatewayAccountPayload.get("type"))
                .map(GatewayAccount.Type::fromString)
                .orElse(GatewayAccount.Type.TEST);
        PaymentProvider paymentProvider = Optional.ofNullable(createGatewayAccountPayload.get("payment_provider"))
                .map(PaymentProvider::fromString)
                .orElse(PaymentProvider.SANDBOX);
        return new GatewayAccount(
                paymentProvider,
                type,
                createGatewayAccountPayload.get("service_name"),
                createGatewayAccountPayload.get("description"),
                createGatewayAccountPayload.get("analytics_id")
        );
    }
}
