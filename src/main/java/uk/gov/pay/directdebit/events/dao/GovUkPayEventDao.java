package uk.gov.pay.directdebit.events.dao;

import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.gatewayaccounts.dao.PaymentProviderOrganisationIdentifierArgumentFactory;
import uk.gov.pay.directdebit.payments.dao.mapper.GoCardlessEventRowMapper;

import java.util.List;
import java.util.Optional;

@RegisterArgumentFactory(PaymentProviderOrganisationIdentifierArgumentFactory.class)
@RegisterRowMapper(GoCardlessEventRowMapper.class)
public interface GovUkPayEventDao {
    
    @SqlUpdate("INSERT INTO govukpay_events(id, event_id, created_at, action, mandate_id)" +
            "VALUES (:id, :eventId, :createdAt, :action, :mandateId)")
    @GetGeneratedKeys
    Long insert(@BindBean GovUkPayEvent govUkPayEvent);
    
    @SqlQuery("SELECT * FROM gocardless_events WHERE mandate_id = :mandateId ORDER BY created_at DESC")
    List<GoCardlessEvent> findEventsForMandateLatestFirst(@Bind("mandateId") String goCardlessMandateIdOrReferenceOrWhateverItsCalled);

    @SqlQuery("SELECT * FROM gocardless_events WHERE mandate_id = :mandateId ORDER BY created_at DESC LIMIT 1")
    Optional<GoCardlessEvent> findLatestEventForMandate(@Bind("mandateId") String mandateId);
}
