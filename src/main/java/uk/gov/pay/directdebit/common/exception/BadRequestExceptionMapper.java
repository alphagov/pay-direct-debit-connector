package uk.gov.pay.directdebit.common.exception;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BadRequestException.class);

    @Override
    public Response toResponse(BadRequestException exception) {
        LOGGER.error(exception.getMessage());
        ImmutableMap<String, String> entity = ImmutableMap.of("message", exception.getMessage());
        return Response.status(400).entity(entity).type(APPLICATION_JSON).build();
    }
}
