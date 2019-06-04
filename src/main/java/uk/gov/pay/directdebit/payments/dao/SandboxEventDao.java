package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payments.dao.mapper.SandboxEventMapper;
import uk.gov.pay.directdebit.payments.model.SandboxEvent;

@RegisterRowMapper(SandboxEventMapper.class)
public interface SandboxEventDao {
    
    @SqlUpdate("INSERT INTO sandbox_events(created_at, mandate_id, payment_id, event_action, event_cause) " +
            "VALUES (:createdAt, :mandateId, :paymentId, :eventAction, :eventCause)")
    @GetGeneratedKeys
    Long insert(@BindBean SandboxEvent sandboxEvent);
}
