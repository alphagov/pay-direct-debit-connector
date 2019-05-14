package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payments.dao.mapper.GoCardlessEventRowMapper;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;

import java.util.List;

@RegisterRowMapper(GoCardlessEventRowMapper.class)
public interface GoCardlessEventDao {
    
    @SqlUpdate("INSERT INTO gocardless_events(event_id, action, resource_type, json, created_at,  details_reason_code, details_scheme, mandate_id, customer_id, "
            + " new_mandate_id, organisation_identifier, parent_event_id, payment_id, payout_id, previous_customer_bank_account_id, refund_id, subscription_id)" +
            "VALUES (:goCardlessEventId, :action, :resourceType, CAST(:json as jsonb), :createdAt, :detailsReasonCode, :detailsScheme, :mandateId," +
            ":customerId, :newMandateId, :organisationIdentifier, :parentEventId, :paymentId, :payoutId, :previousCustomerBankAccountId, :refundId," +
            ":subscriptionId)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessEvent goCardlessEvent);
    
    @SqlQuery("SELECT * FROM gocardless_events WHERE mandate_id = :mandateId")
    List<GoCardlessEvent> findEventsForMandate(@Bind("mandateId") String mandateId);
}
