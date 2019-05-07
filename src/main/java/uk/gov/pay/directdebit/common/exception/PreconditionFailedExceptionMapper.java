package uk.gov.pay.directdebit.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.common.model.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class PreconditionFailedExceptionMapper implements ExceptionMapper<PreconditionFailedException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreconditionFailedException.class);

    @Override
    public Response toResponse(PreconditionFailedException exception) {
        LOGGER.error(exception.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(exception.getErrorIdentifier(), exception.getMessage());
        return Response.status(412).entity(errorResponse).type(APPLICATION_JSON).build();
    }
}
