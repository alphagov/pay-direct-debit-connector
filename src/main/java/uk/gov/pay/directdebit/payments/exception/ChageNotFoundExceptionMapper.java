package uk.gov.pay.directdebit.payments.exception;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ChageNotFoundExceptionMapper implements ExceptionMapper<ChargeNotFoundException> {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(ChageNotFoundExceptionMapper.class);

    /**
     * Map an exception to a {@link Response}.
     *
     * @param exception the exception to map to a response.
     * @return a response mapped from the supplied exception.
     */
    @Override
    public Response toResponse(ChargeNotFoundException exception) {
        LOGGER.error(exception.getMessage());

        return Response.status(500).build();
    }

}
