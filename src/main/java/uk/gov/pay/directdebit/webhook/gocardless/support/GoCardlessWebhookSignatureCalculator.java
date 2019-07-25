package uk.gov.pay.directdebit.webhook.gocardless.support;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.google.common.io.BaseEncoding.base16;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Calculates a webhook signature in the format GoCardless use (lower-case hexadecimal HMAC SHA-256 digest of the UTF-8 bytes of the supplied body)
 *
 * @see <a href="https://developer.gocardless.com/api-reference/#webhooks-signing-webhooks">Signing webhooks</a>
 * @see <a href="https://developer.gocardless.com/getting-started/partners/staying-up-to-date-with-webhooks/#building-your-first-webhook-handler">Building your first webhook handler</a>
 * @see <a href="https://github.com/gocardless/gocardless-pro-java-example">GoCardless Pro Java client library example</a>
 */
public class GoCardlessWebhookSignatureCalculator {

    private final SecretKeySpec hmacSha256SecretKeySpec;

    public GoCardlessWebhookSignatureCalculator(String secretKey) {
        this.hmacSha256SecretKeySpec = new SecretKeySpec(secretKey.getBytes(UTF_8), "HmacSHA256");
    }

    public String calculate(String body) {
        Mac hmacSha256 = createHmacSha256WithSecretKey();
        byte[] sha256Signature = hmacSha256.doFinal(body.getBytes(UTF_8));
        return base16().lowerCase().encode(sha256Signature);
    }

    private Mac createHmacSha256WithSecretKey() {
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacSha256SecretKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
        return mac;
    }

}
