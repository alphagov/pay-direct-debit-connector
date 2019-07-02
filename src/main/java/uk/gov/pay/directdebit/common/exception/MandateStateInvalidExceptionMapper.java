package uk.gov.pay.directdebit.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.directdebit.common.model.ErrorResponse;
import uk.gov.pay.directdebit.mandate.exception.MandateStateInvalidException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class MandateStateInvalidExceptionMapper implements ExceptionMapper<MandateStateInvalidException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MandateStateInvalidExceptionMapper.class);

    @Override
    public Response toResponse(MandateStateInvalidException exception) {
        LOGGER.error(exception.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ErrorIdentifier.GENERIC, exception.getMessage());
        return Response.status(500).entity(errorResponse).type(APPLICATION_JSON).build();
    }
}
