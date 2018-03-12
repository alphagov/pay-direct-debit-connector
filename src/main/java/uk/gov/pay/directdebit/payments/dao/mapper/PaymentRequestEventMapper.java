package uk.gov.pay.directdebit.payments.dao.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class PaymentRequestEventMapper implements ResultSetMapper<PaymentRequestEvent> {
    private static final String ID_COLUMN = "id";
    private static final String PAYMENT_REQUEST_ID_COLUMN = "payment_request_id";
    private static final String EVENT_TYPE_COLUMN = "event_type";
    private static final String EVENT_COLUMN = "event";
    private static final String EVENT_DATE_COLUMN = "event_date";

    @Override
    public PaymentRequestEvent map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new PaymentRequestEvent(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(PAYMENT_REQUEST_ID_COLUMN),
                PaymentRequestEvent.Type.valueOf(resultSet.getString(EVENT_TYPE_COLUMN)),
                PaymentRequestEvent.SupportedEvent.valueOf(resultSet.getString(EVENT_COLUMN)),
                ZonedDateTime.ofInstant(resultSet.getTimestamp(EVENT_DATE_COLUMN).toInstant(), ZoneOffset.UTC));
    }
}
