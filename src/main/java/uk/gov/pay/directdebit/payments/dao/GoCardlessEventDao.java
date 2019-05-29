package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payments.dao.mapper.GoCardlessEventMapper;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEventIdArgumentFactory;

@RegisterArgumentFactory(GoCardlessEventIdArgumentFactory.class)
@RegisterRowMapper(GoCardlessEventMapper.class)
public interface GoCardlessEventDao {

    @SqlUpdate("INSERT INTO gocardless_events(internal_event_id, event_id, action, resource_type, json," +
            " detailsCause," +
            " detailsDescription," +
            " detailsOrigin," +
            " detailsReasonCode," +
            " detailsScheme," +
            " linksMandate," +
            " linksNewCustomerBankAccount," +
            " linksNewMandate," +
            " linksOrganisation," +
            " linksParentEvent," +
            " linksPayment," +
            " linksPayout," +
            " linksPreviousCustomerBankAccount," +
            " linksRefund," +
            " linksSubscription," +
            " created_at, details_cause)" +
            " VALUES (:eventId, :goCardlessEventId, :action, :resourceType, CAST(:json as jsonb)," +
            " :detailsCause," +
            " :detailsDescription," +
            " :detailsOrigin," +
            " :detailsReasonCode," +
            " :detailsScheme," +
            " :linksMandate," +
            " :linksNewCustomerBankAccount," +
            " :linksNewMandate," +
            " :linksOrganisation," +
            " :linksParentEvent," +
            " :linksPayment," +
            " :linksPayout," +
            " :linksPreviousCustomerBankAccount," +
            " :linksRefund," +
            " :linksSubscription," +
            " :createdAt)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessEvent goCardlessEvent);

    @SqlUpdate("UPDATE gocardless_events t SET internal_event_id = :eventId WHERE t.id = :id")
    int updateEventId(@Bind("id") Long id, @Bind("eventId") Long eventId);
}
