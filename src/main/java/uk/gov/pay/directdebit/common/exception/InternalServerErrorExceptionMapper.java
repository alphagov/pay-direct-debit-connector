package uk.gov.pay.directdebit.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.directdebit.common.model.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class InternalServerErrorExceptionMapper implements ExceptionMapper<InternalServerErrorException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalServerErrorException.class);

    @Override
    public Response toResponse(InternalServerErrorException exception) {
        LOGGER.error(exception.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ErrorIdentifier.GENERIC, exception.getMessage());
        return Response.status(500).entity(errorResponse).type(APPLICATION_JSON).build();
    }
}
