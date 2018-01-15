package uk.gov.pay.directdebit.common.exception;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(ConflictException.class);

    @Override
    public Response toResponse(ConflictException exception) {
        LOGGER.error(exception.getMessage());
        ImmutableMap<String, String> entity = ImmutableMap.of("message", exception.getMessage());
        return Response.status(409).entity(entity).type(APPLICATION_JSON).build();
    }
}
