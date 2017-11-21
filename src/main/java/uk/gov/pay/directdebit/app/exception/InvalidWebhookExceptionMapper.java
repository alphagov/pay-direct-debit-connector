package uk.gov.pay.directdebit.app.exception;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class InvalidWebhookExceptionMapper implements ExceptionMapper<InvalidWebhookException> {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(InvalidWebhookExceptionMapper.class);

    @Override
    public Response toResponse(InvalidWebhookException exception) {
        // Log an error and possibly do SumoLogic alert
        LOGGER.error(exception.getMessage());

        // Webhooks with an invalid signature must return a "498 Token Invalid" error.
        // see https://developer.gocardless.com/api-reference/#webhooks-overview
        return Response.status(498).build();
    }

}
