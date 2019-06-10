package uk.gov.pay.directdebit.common.exception;

import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.directdebit.common.model.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class UnlinkedGCMerchantAccountExceptionMapper implements ExceptionMapper<UnlinkedGCMerchantAccountException> {
    @Override
    public Response toResponse(UnlinkedGCMerchantAccountException e) {
        ErrorResponse errorResponse =
                new ErrorResponse(ErrorIdentifier.GO_CARDLESS_ACCOUNT_NOT_LINKED, e.getMessage());
        return Response.status(Response.Status.FORBIDDEN).entity(errorResponse).build();
    }
}
