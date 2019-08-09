package uk.gov.pay.directdebit.common.exception;

import uk.gov.pay.commons.model.ErrorIdentifier;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class MandateIdInvalidExceptionMapper implements ExceptionMapper<MandateIdInvalidException> {
    
    @Override
    public Response toResponse(MandateIdInvalidException e) {
        return Response.status(409).entity(ErrorIdentifier.MANDATE_ID_INVALID).build();
    }
}
