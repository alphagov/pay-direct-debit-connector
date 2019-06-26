package uk.gov.pay.directdebit.mandate.model.subtype;

import uk.gov.pay.commons.model.WrappedStringValue;

public class MandateExternalId extends WrappedStringValue {

    private MandateExternalId(String mandateExternalId) {
        super(mandateExternalId);
    }

    public static MandateExternalId valueOf(String mandateExternalId) {
        return new MandateExternalId(mandateExternalId);
    }

}
