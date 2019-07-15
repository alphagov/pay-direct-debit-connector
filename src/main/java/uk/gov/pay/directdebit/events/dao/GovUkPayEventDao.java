package uk.gov.pay.directdebit.events.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.events.dao.mapper.GovUkPayEventMapper;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;

import java.util.Optional;

@RegisterRowMapper(GovUkPayEventMapper.class)
public interface GovUkPayEventDao {
    
    @SqlUpdate("INSERT INTO govukpay_events(mandate_id," +
            " payment_id, event_date, resource_type, event_type) VALUES (:mandateId, :paymentId, :eventDate," +
            " :resourceType, :eventType)")
    @GetGeneratedKeys
    Long insert(@BindBean GovUkPayEvent govUkPayEvent);
    
    @SqlQuery("SELECT id, " +
            "mandate_id, " +
            "payment_id, " +
            "event_date, " +
            "resource_type, " +
            "event_type " +
            "FROM govukpay_events " +
            "WHERE mandate_id = :mandateId " +
            "ORDER BY event_date DESC " +
            "LIMIT 1")
    Optional<GovUkPayEvent> findLatestEventForMandate(@Bind("mandateId") Long mandateId);

    @SqlQuery("SELECT id, " +
            "mandate_id, " +
            "payment_id, " +
            "event_date, " +
            "resource_type, " +
            "event_type " +
            "FROM govukpay_events " +
            "WHERE payment_id = :paymentId " +
            "ORDER BY event_date DESC " +
            "LIMIT 1")
    Optional<GovUkPayEvent> findLatestEventForPayment(@Bind("paymentId") Long paymentId);
}
