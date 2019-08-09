package uk.gov.pay.directdebit.tokens.exception;

import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.directdebit.common.exception.NotFoundException;

public class TokenNotFoundException extends NotFoundException {

    public TokenNotFoundException() {
        super("No one-time token found for mandate", ErrorIdentifier.GENERIC);
    }
}
