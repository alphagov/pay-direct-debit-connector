package uk.gov.pay.directdebit.common.exception;

import uk.gov.pay.directdebit.common.model.ErrorResponse;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class GoCardlessAccountAlreadyConnectedExceptionMapper implements ExceptionMapper<GoCardlessAccountAlreadyConnectedException> {
    @Override
    public Response toResponse(GoCardlessAccountAlreadyConnectedException exception) {
        ErrorResponse errorResponse =
                new ErrorResponse(ErrorIdentifier.GO_CARDLESS_ACCOUNT_ALREADY_LINKED_TO_ANOTHER_ACCOUNT, exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }
}
