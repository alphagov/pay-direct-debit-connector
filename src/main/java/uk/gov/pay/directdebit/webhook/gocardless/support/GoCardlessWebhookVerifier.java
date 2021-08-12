package uk.gov.pay.directdebit.webhook.gocardless.support;

import uk.gov.pay.directdebit.webhook.gocardless.exception.InvalidWebhookException;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static java.lang.String.format;

public class GoCardlessWebhookVerifier {

    private final GoCardlessWebhookSignatureCalculator goCardlessWebhookSignatureCalculator;

    @Inject
    GoCardlessWebhookVerifier(GoCardlessWebhookSignatureCalculator goCardlessWebhookSignatureCalculator) {
        this.goCardlessWebhookSignatureCalculator = goCardlessWebhookSignatureCalculator;
    }

    public void verify(String body, String signature) {
        verifySignature(body, signature);
    }

    private void verifySignature(String body, String expectedSignature) {
        String computedSignature = goCardlessWebhookSignatureCalculator.calculate(body);

        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), computedSignature.getBytes(StandardCharsets.UTF_8))) {
            throw new InvalidWebhookException(format("Invalid GoCardless webhook signature, received %s but computed %s",
                    expectedSignature, computedSignature));
        }
    }

}
