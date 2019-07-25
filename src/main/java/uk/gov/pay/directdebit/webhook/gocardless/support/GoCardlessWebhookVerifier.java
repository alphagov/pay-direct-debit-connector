package uk.gov.pay.directdebit.webhook.gocardless.support;

import org.apache.commons.lang3.StringUtils;
import uk.gov.pay.directdebit.webhook.gocardless.exception.InvalidWebhookException;

import javax.inject.Inject;

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

        if (!StringUtils.equals(expectedSignature, computedSignature)) {
            throw new InvalidWebhookException(format("Invalid GoCardless webhook signature, received %s but computed %s",
                    expectedSignature, computedSignature));
        }
    }

}
