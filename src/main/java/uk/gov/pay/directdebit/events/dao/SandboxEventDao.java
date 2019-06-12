package uk.gov.pay.directdebit.events.dao;

import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateIdArgumentFactory;

@RegisterRowMapper(SandboxEventMapper.class)
@RegisterArgumentFactory(SandboxMandateIdArgumentFactory.class)
public interface SandboxEventDao {
    
    @SqlUpdate("INSERT INTO sandbox_events(created_at, mandate_id, payment_id, event_action, event_cause) " +
            "VALUES (:createdAt, :mandateId, :paymentId, :eventAction, :eventCause)")
    @GetGeneratedKeys
    Long insert(@BindBean SandboxEvent sandboxEvent);
}
