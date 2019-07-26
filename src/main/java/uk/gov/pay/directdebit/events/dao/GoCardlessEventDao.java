package uk.gov.pay.directdebit.events.dao;

import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.events.dao.mapper.GoCardlessEventMapper;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GoCardlessEventIdArgumentFactory;
import uk.gov.pay.directdebit.events.model.GoCardlessOrganisationIdArgumentFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateIdArgumentFactory;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentIdArgumentFactory;

import java.util.Optional;
import java.util.Set;

@RegisterArgumentFactory(GoCardlessEventIdArgumentFactory.class)
@RegisterArgumentFactory(GoCardlessMandateIdArgumentFactory.class)
@RegisterArgumentFactory(GoCardlessPaymentIdArgumentFactory.class)
@RegisterArgumentFactory(GoCardlessOrganisationIdArgumentFactory.class)
@RegisterRowMapper(GoCardlessEventMapper.class)
public interface GoCardlessEventDao {

    @SqlUpdate("INSERT INTO gocardless_events(" +
            " event_id," +
            " action," +
            " resource_type," +
            " json," +
            " details_cause," +
            " details_description," +
            " details_origin," +
            " details_reason_code," +
            " details_scheme," +
            " links_mandate," +
            " links_new_customer_bank_account," +
            " links_new_mandate," +
            " links_organisation," +
            " links_parent_event," +
            " links_payment," +
            " links_payout," +
            " links_previous_customer_bank_account," +
            " links_refund," +
            " links_subscription," +
            " created_at)" +
            " VALUES (" +
            " :goCardlessEventId," +
            " :action," +
            " :resourceType," +
            " CAST(:json as jsonb)," +
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

    @SqlQuery("SELECT id, " +
            "internal_event_id, " +
            "event_id, " +
            "action, " +
            "created_at, " +
            "details_cause, " +
            "details_description, " +
            "details_origin, " +
            "details_reason_code, " +
            "details_scheme, " +
            "resource_type," +
            "links_mandate, " +
            "links_new_customer_bank_account, " +
            "links_new_mandate, " +
            "links_organisation, " +
            "links_parent_event, " +
            "links_payment, " +
            "links_payout, " +
            "links_previous_customer_bank_account, " +
            "links_refund, " +
            "links_subscription, " +
            "json, " +
            "event_id " +
            "FROM gocardless_events " +
            "WHERE links_mandate = :goCardlessMandateId " +
            "AND links_organisation = :goCardlessOrganisationId " +
            "AND action IN (<applicableActions>) " +
            "ORDER BY created_at DESC " +
            "LIMIT 1")
    Optional<GoCardlessEvent> findLatestApplicableEventForMandate(@Bind("goCardlessMandateId") GoCardlessMandateId goCardlessMandateId,
                                                                  @Bind("goCardlessOrganisationId") GoCardlessOrganisationId goCardlessOrganisationId,
                                                                  @BindList("applicableActions") Set<String> applicableActions);

    @SqlQuery("SELECT id, " +
            "internal_event_id, " +
            "event_id, " +
            "action, " +
            "created_at, " +
            "details_cause, " +
            "details_description, " +
            "details_origin, " +
            "details_reason_code, " +
            "details_scheme, " +
            "resource_type," +
            "links_mandate, " +
            "links_new_customer_bank_account, " +
            "links_new_mandate, " +
            "links_organisation, " +
            "links_parent_event, " +
            "links_payment, " +
            "links_payout, " +
            "links_previous_customer_bank_account, " +
            "links_refund, " +
            "links_subscription, " +
            "json, " +
            "event_id " +
            "FROM gocardless_events " +
            "WHERE links_payment = :goCardlessPaymentId " +
            "AND links_organisation = :goCardlessOrganisationId " +
            "AND action IN (<applicableActions>) " +
            "ORDER BY created_at DESC " +
            "LIMIT 1")
    Optional<GoCardlessEvent> findLatestApplicableEventForPayment(@Bind("goCardlessPaymentId") GoCardlessPaymentId goCardlessPaymentId,
                                                                  @Bind("goCardlessOrganisationId") GoCardlessOrganisationId goCardlessOrganisationId,
                                                                  @BindList("applicableActions") Set<String> applicableActions);

}
