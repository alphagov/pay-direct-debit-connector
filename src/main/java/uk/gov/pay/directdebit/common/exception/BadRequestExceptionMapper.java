package uk.gov.pay.directdebit.common.exception;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {
    /**
     * Map an exception to a {@link Response}.
     *
     * @param exception the exception to map to a response.
     * @return a response mapped from the supplied exception.
     */
    @Override
    public Response toResponse(BadRequestException exception) {
        ImmutableMap<String, String> entity = ImmutableMap.of("message", exception.getMessage());
        return Response.status(400).entity(entity).build();
    }

}
