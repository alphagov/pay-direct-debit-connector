package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payments.dao.mapper.GoCardlessEventMapper;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;

@RegisterRowMapper(GoCardlessEventMapper.class)
public interface GoCardlessEventDao {

    @SqlUpdate("INSERT INTO gocardless_events(payment_request_events_id, event_id, action, resource_type, json, created_at) VALUES (:eventId, :goCardlessEventId, :action, :resourceType, CAST(:json as jsonb), :createdAt)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessEvent goCardlessEvent);
    
    @SqlUpdate("UPDATE gocardless_events t SET payment_request_events_id = :eventId WHERE t.id = :id")
    int updateEventId(@Bind("id") Long id, @Bind("eventId") Long eventId);
}
