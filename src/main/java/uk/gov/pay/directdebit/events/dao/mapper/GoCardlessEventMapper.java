package uk.gov.pay.directdebit.events.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GoCardlessEventId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.events.model.GoCardlessResourceType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

public class GoCardlessEventMapper implements RowMapper<GoCardlessEvent> {

    @Override
    public GoCardlessEvent map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        var builder = GoCardlessEvent.GoCardlessEventBuilder.aGoCardlessEvent()
                .withId(resultSet.getLong("id"))
                .withInternalEventId(resultSet.getLong("internal_event_id"))
                .withGoCardlessEventId(GoCardlessEventId.valueOf(resultSet.getString("event_id")))
                .withAction(resultSet.getString("action"))
                .withResourceType(GoCardlessResourceType.fromString(resultSet.getString("resource_type")))
                .withJson(resultSet.getString("json"))
                .withDetailsCause(resultSet.getString("details_cause"))
                .withDetailsDescription(resultSet.getString("details_description"))
                .withDetailsOrigin(resultSet.getString("details_origin"))
                .withDetailsReasonCode(resultSet.getString("details_reason_code"))
                .withDetailsScheme(resultSet.getString("details_scheme"))
                .withLinksNewCustomerBankAccount(resultSet.getString("links_new_customer_bank_account"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf(resultSet.getString("links_organisation")))
                .withLinksParentEvent(resultSet.getString("links_parent_event"))
                .withLinksPayout(resultSet.getString("links_payout"))
                .withLinksPreviousCustomerBankAccount(resultSet.getString("links_previous_customer_bank_account"))
                .withLinksRefund(resultSet.getString("links_refund"))
                .withLinksSubscription(resultSet.getString("links_subscription"))
                .withCreatedAt(ZonedDateTime.ofInstant(
                        resultSet.getTimestamp("created_at").toInstant(), ZoneOffset.UTC));

        Optional.ofNullable(resultSet.getString("links_payment"))
                .map(GoCardlessPaymentId::valueOf)
                .ifPresent(builder::withLinksPayment);

        Optional.ofNullable(resultSet.getString("links_mandate"))
                .map(GoCardlessMandateId::valueOf)
                .ifPresent(builder::withLinksMandate);

        Optional.ofNullable(resultSet.getString("links_new_mandate"))
                .map(GoCardlessMandateId::valueOf)
                .ifPresent(builder::withLinksNewMandate);

        return builder.build();
    }
}
