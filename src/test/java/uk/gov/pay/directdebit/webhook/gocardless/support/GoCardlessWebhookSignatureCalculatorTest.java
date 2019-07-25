package uk.gov.pay.directdebit.webhook.gocardless.support;

import org.junit.Test;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @see <a href="https://tools.ietf.org/html/rfc4231#section-4.2">RFC 4231 § 4.2</a>
 */
public class GoCardlessWebhookSignatureCalculatorTest {

    private GoCardlessWebhookSignatureCalculator goCardlessWebhookSignatureCalculator;

    @Test
    public void calculateReturnsLowerCaseHexadecimalHmacSha256DigestOfInputString() {
        byte[] secretKeyBytes = new byte[20];
        Arrays.fill(secretKeyBytes, (byte) 0xb);
        String secretKey = new String(secretKeyBytes, UTF_8);
        
        goCardlessWebhookSignatureCalculator = new GoCardlessWebhookSignatureCalculator(secretKey);

        String signature = goCardlessWebhookSignatureCalculator.calculate("Hi There");

        assertThat(signature, is("b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7"));
    }

}
