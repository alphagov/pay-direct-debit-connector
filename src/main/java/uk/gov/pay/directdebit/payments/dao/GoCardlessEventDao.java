package uk.gov.pay.directdebit.payments.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.pay.directdebit.common.dao.DateArgumentFactory;
import uk.gov.pay.directdebit.payments.dao.mapper.GoCardlessEventMapper;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;

import java.util.Optional;

@RegisterMapper(GoCardlessEventMapper.class)
public interface GoCardlessEventDao {

    @SqlUpdate("INSERT INTO gocardless_events(payment_request_events_id, event_id, action, resource_type, json, created_at) VALUES (:paymentRequestEventId, :eventId, :action, :resourceType, CAST(:json as jsonb), :createdAt)")
    @GetGeneratedKeys
    @RegisterArgumentFactory(DateArgumentFactory.class)
    Long insert(@BindBean GoCardlessEvent goCardlessEvent);

    @SqlQuery("SELECT * FROM gocardless_events g WHERE g.id = :id")
    @SingleValueResult(GoCardlessEvent.class)
    Optional<GoCardlessEvent> findById(@Bind("id") Long id);

    @SqlUpdate("UPDATE gocardless_events t SET payment_request_events_id = :eventId WHERE t.id = :id")
    int updatePaymentRequestEventId(@Bind("id") Long id, @Bind("eventId") Long eventId);
}
