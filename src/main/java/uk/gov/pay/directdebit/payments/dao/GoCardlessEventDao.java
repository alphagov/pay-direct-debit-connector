package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payments.dao.mapper.GoCardlessEventMapper;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;

import java.util.Optional;

@RegisterRowMapper(GoCardlessEventMapper.class)
public interface GoCardlessEventDao {

    @SqlUpdate("INSERT INTO gocardless_events(payment_request_events_id, event_id, action, resource_type, json, created_at) VALUES (:paymentRequestEventId, :eventId, :action, :resourceType, CAST(:json as jsonb), :createdAt)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessEvent goCardlessEvent);

    @SqlQuery("SELECT * FROM gocardless_events g WHERE g.id = :id")
    Optional<GoCardlessEvent> findById(@Bind("id") Long id);

    @SqlUpdate("UPDATE gocardless_events t SET payment_request_events_id = :eventId WHERE t.id = :id")
    int updatePaymentRequestEventId(@Bind("id") Long id, @Bind("eventId") Long eventId);
}
