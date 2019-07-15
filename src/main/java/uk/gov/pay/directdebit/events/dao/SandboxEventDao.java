package uk.gov.pay.directdebit.events.dao;

import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateIdArgumentFactory;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentIdArgumentFactory;

import java.util.Optional;
import java.util.Set;

@RegisterRowMapper(SandboxEventMapper.class)
@RegisterArgumentFactory(SandboxMandateIdArgumentFactory.class)
@RegisterArgumentFactory(SandboxPaymentIdArgumentFactory.class)
public interface SandboxEventDao {
    
    @SqlUpdate("INSERT INTO sandbox_events(created_at, mandate_id, payment_id, event_action, event_cause) " +
            "VALUES (:createdAt, :mandateId, :paymentId, :eventAction, :eventCause)")
    @GetGeneratedKeys
    Long insert(@BindBean SandboxEvent sandboxEvent);

    @SqlQuery("SELECT id, " +
            "created_at, " +
            "mandate_id, " +
            "payment_id, " +
            "event_action, " +
            "event_cause " +
            "FROM sandbox_events " +
            "WHERE payment_id = :paymentId " +
            "AND event_action IN (<applicableActions>) " +
            "ORDER BY created_at DESC " +
            "LIMIT 1")
    Optional<SandboxEvent> findLatestApplicableEventForPayment(@Bind("paymentId") SandboxPaymentId sandboxPaymentId,
                                                               @BindList("applicableActions") Set<String> applicableActions);
}
