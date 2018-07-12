package uk.gov.pay.directdebit.common.exception;

import com.google.common.collect.ImmutableMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class PreconditionFailedExceptionMapper implements ExceptionMapper<PreconditionFailedException> {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(PreconditionFailedException.class);

    @Override
    public Response toResponse(PreconditionFailedException exception) {
        LOGGER.error(exception.getMessage());
        ImmutableMap<String, String> entity = ImmutableMap.of("message", exception.getMessage());
        return Response.status(412).entity(entity).type(APPLICATION_JSON).build();
    }
}
