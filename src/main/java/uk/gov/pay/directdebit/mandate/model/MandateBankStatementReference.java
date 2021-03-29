package uk.gov.pay.directdebit.mandate.model;

import uk.gov.service.payments.commons.model.WrappedStringValue;

/**
 * Reference for a mandate, which may be shown on the paying user’s bank
 * statement (possibly truncated) when a payment is taken
 * <br>
 * Since the BACS rules for valid mandate references are complex, we
 * generally let GoCardless generate them for us
 * <br>
 * This object itself does no validation, so instances are not guaranteed to
 * represent valid mandate references according to the BACS rules
 * 
 * @see <a href="https://developer.gocardless.com/api-reference/#core-endpoints-mandates">GoCardless Mandates</a>
 * @see <a href="https://developer.gocardless.com/api-reference/#appendix-character-sets">GoCardless character sets</a>
 * @see <a href="https://support.gocardless.com/hc/en-gb/articles/360000962309">GoCardless customer bank statement references</a>
 */
public class MandateBankStatementReference extends WrappedStringValue {

    private MandateBankStatementReference(String mandateBankStatementReference) {
        super(mandateBankStatementReference);
    }

    public static MandateBankStatementReference valueOf(String mandateBankStatementReference) {
        return new MandateBankStatementReference(mandateBankStatementReference);
    }

}
