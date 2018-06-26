package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payments.dao.mapper.EventMapper;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;

import java.util.Optional;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type;

@RegisterRowMapper(EventMapper.class)
public interface DirectDebitEventDao {

    @SqlUpdate("INSERT INTO events(mandate_id, transaction_id, event_type, event, event_date) VALUES (:mandateId, :transactionId, :eventType, :event, :eventDate)")
    @GetGeneratedKeys
    Long insert(@BindBean DirectDebitEvent directDebitEvent);

    @SqlQuery("SELECT * FROM events e WHERE e.mandate_id = :mandateId and e.event_type = :eventType and e.event = :event")
    Optional<DirectDebitEvent> findByMandateIdAndEvent(@Bind("mandateId") Long mandateId, @Bind("eventType") Type eventType, @Bind("event") SupportedEvent event);
}
