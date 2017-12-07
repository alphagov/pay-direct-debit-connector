package uk.gov.pay.directdebit.common.exception;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
    /**
     * Map an exception to a {@link Response}.
     *
     * @param exception the exception to map to a response.
     * @return a response mapped from the supplied exception.
     */
    @Override
    public Response toResponse(NotFoundException exception) {
        ImmutableMap<String, String> entity = ImmutableMap.of("message", exception.getMessage());
        return Response.status(404).entity(entity).build();
    }

}
