package uk.gov.pay.directdebit.common.exception;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotFoundException.class);

    @Override
    public Response toResponse(NotFoundException exception) {
        LOGGER.error(exception.getMessage());
        ImmutableMap<String, String> entity = ImmutableMap.of("message", exception.getMessage());
        return Response.status(404).entity(entity).type(APPLICATION_JSON).build();
    }
}
