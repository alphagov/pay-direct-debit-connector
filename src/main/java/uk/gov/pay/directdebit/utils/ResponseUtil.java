package uk.gov.pay.directdebit.utils;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.status;

public class ResponseUtil {
    protected static final Logger logger = LoggerFactory.getLogger(ResponseUtil.class);

    public static Response badRequestResponse(List message) {
        logger.error(message.toString());
        return responseWithMessageMap(BAD_REQUEST, message);
    }

    private static Response responseWithMessageMap(Response.Status status, Object entity) {
        return responseWithEntity(status, ImmutableMap.of("message", entity));
    }

    private static Response responseWithEntity(Response.Status status, Object entity) {
        return status(status).entity(entity).build();
    }

    public static Response notFoundResponse(String message) {
        logger.error(message);
        return responseWithMessageMap(NOT_FOUND, message);
    }
}
