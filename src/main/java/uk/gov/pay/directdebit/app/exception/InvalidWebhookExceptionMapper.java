package uk.gov.pay.directdebit.app.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class InvalidWebhookExceptionMapper implements ExceptionMapper<InvalidWebhookException> {

    @Override
    public Response toResponse(InvalidWebhookException exception) {
        // Webhooks with an invalid signature must return a "498 Token Invalid" error.
        // see https://developer.gocardless.com/api-reference/#webhooks-overview
        return Response.status(498).build();
    }

}
