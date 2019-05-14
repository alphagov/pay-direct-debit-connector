package uk.gov.pay.directdebit.payments.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;

public class GoCardlessEventRowMapper implements RowMapper<GoCardlessEvent> {

    @Override
    public GoCardlessEvent map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return GoCardlessEvent.GoCardlessEventBuilder.aGoCardlessEvent()
                .withAction(resultSet.getString("action"))
                .withCreatedAt(ZonedDateTime.ofInstant(resultSet.getTimestamp("created_at").toInstant(), ZoneOffset.UTC))
                .withCustomerId(resultSet.getString("customer_id"))
                .withDetailsCause(resultSet.getString("details_cause"))
                .withDetailsDescription(resultSet.getString("details_description"))
                .withDetailsOrigin(resultSet.getString("details_origin"))
                .withDetailsReasonCode(resultSet.getString("details_reason_code"))
                .withDetailsScheme(resultSet.getString("details_scheme"))
                .withGoCardlessEventId(resultSet.getString("gocardless_event_id"))
                .withId(resultSet.getLong("id"))
                .withJson(resultSet.getString("json"))
                .withMandateId(resultSet.getString("mandate_id"))
                .withNewMandateId(resultSet.getString("new_mandate_id"))
                .withOrganisationIdentifier(PaymentProviderOrganisationIdentifier.of(resultSet.getString("organisation_id")))
                .withParentEventId(resultSet.getString("parent_event_id"))
                .withPaymentId(resultSet.getString("payment_id"))
                .withPayoutId(resultSet.getString("payout_id"))
                .withResourceId(resultSet.getString("resource_id"))
                .withSubscriptionId(resultSet.getString("subscription_id"))
                .withPreviousCustomerBankAccountId(resultSet.getString("previous_customer_bank_account"))
                .withRefundId(resultSet.getString("refund_id"))
                .withResourceType(GoCardlessResourceType.fromString(resultSet.getString("resource_type")))
                .build();
    }
}
