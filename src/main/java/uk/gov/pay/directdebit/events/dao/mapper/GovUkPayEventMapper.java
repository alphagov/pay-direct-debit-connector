package uk.gov.pay.directdebit.events.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

public class GovUkPayEventMapper implements RowMapper<GovUkPayEvent> {
    @Override
    public GovUkPayEvent map(ResultSet rs, StatementContext ctx) throws SQLException {
        var builder = GovUkPayEvent.GovUkPayEventBuilder.aGovUkPayEvent()
                .withId(rs.getLong("id"))
                .withResourceType(GovUkPayEvent.ResourceType.valueOf(rs.getString("resource_type")))
                .withEventDate(ZonedDateTime.ofInstant(
                        rs.getTimestamp("event_date").toInstant(), ZoneOffset.UTC))
                .withEventType(GovUkPayEvent.GovUkPayEventType.valueOf(rs.getString("event_type")));

        Optional.ofNullable(rs.getObject("mandate_id"))
                .map(mandateId -> (Long)mandateId)
                .ifPresent(builder::withMandateId);
        
        Optional.ofNullable(rs.getObject("payment_id"))
                .map(paymentId -> (Long)paymentId)
                .ifPresent(builder::withPaymentId);
        
        return builder.build();
    }
}
