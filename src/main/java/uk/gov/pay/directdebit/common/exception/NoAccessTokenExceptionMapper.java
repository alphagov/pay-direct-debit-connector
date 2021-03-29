package uk.gov.pay.directdebit.common.exception;

import uk.gov.pay.directdebit.common.model.ErrorResponse;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class NoAccessTokenExceptionMapper implements ExceptionMapper<NoAccessTokenException> {
    @Override
    public Response toResponse(NoAccessTokenException exception) {
        ErrorResponse errorResponse = new ErrorResponse(ErrorIdentifier.GO_CARDLESS_ACCOUNT_NOT_LINKED,
                exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
    }
}
