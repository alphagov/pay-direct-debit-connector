package uk.gov.pay.directdebit.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.directdebit.common.model.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotFoundException.class);

    @Override
    public Response toResponse(NotFoundException exception) {
        LOGGER.error(exception.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ErrorIdentifier.GENERIC, exception.getMessage());
        return Response.status(404).entity(errorResponse).type(APPLICATION_JSON).build();
    }
}
