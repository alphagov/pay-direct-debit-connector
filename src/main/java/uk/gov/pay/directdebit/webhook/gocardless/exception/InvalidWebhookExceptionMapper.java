package uk.gov.pay.directdebit.webhook.gocardless.exception;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class InvalidWebhookExceptionMapper implements ExceptionMapper<InvalidWebhookException> {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(InvalidWebhookExceptionMapper.class);

    /**
     * Map an exception to a {@link javax.ws.rs.core.Response}.
     * Webhooks with an invalid signature must return a "498 Token Invalid" error.
     *
     * @param exception the exception to map to a response.
     * @return a response mapped from the supplied exception.
     * @see <a href="https://developer.gocardless.com/api-reference/#webhooks-overview">https://developer.gocardless.com/api-reference/#webhooks-overview</a>
     */
    @Override
    public Response toResponse(InvalidWebhookException exception) {
        // Log an error and possibly do SumoLogic alert
        LOGGER.error(exception.getMessage());

        return Response.status(498).build();
    }

}
