package uk.gov.pay.directdebit.payments.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEventId;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class GoCardlessEventMapper implements RowMapper<GoCardlessEvent> {
    private static final String ID_COLUMN = "id";
    private static final String EVENT_ID_COLUMN = "event_id";
    private static final String GOCARDLESS_EVENT_ID_COLUMN = "gocardless_event_id";
    private static final String ACTION_COLUMN = "action";
    private static final String RESOURCE_TYPE_COLUMN = "resource_type";
    private static final String JSON_COLUMN = "json";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String DETAILS_CAUSE_COLUMN = "details_cause";
    private static final String DETAILS_DESCRIPTION_COLUMN = "details_description";
    private static final String DETAILS_ORIGIN_COLUMN = "details_origin";
    private static final String DETAILS_REASON_CODE_COLUMN = "details_reason_code";
    private static final String DETAILS_SCHEME_COLUMN = "details_scheme";
    private static final String LINKS_MANDATE_COLUMN = "links_mandate";
    private static final String LINKS_NEW_CUSTOMER_BANK_ACCOUNT_COLUMN = "links_new_customer_bank_account";
    private static final String LINKS_NEW_MANDATE_COLUMN = "links_new_mandate";
    private static final String LINKS_ORGANISATION_COLUMN = "links_organisation";
    private static final String LINKS_PARENT_EVENT_COLUMN = "links_parent_event";
    private static final String LINKS_PAYMENT_COLUMN = "links_payment";
    private static final String LINKS_PAYOUT_COLUMN = "links_payout";
    private static final String LINKS_PREVIOUS_CUSTOMER_BANK_ACCOUNT_COLUMN = "links_previous_customer_bank_account";
    private static final String LINKS_REFUND_COLUMN = "links_refund";
    private static final String LINKS_SUBSCRIPTION_COLUMN = "links_subscription";

    @Override
    public GoCardlessEvent map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new GoCardlessEvent(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(EVENT_ID_COLUMN),
                GoCardlessEventId.valueOf(resultSet.getString(GOCARDLESS_EVENT_ID_COLUMN)),
                resultSet.getString(ACTION_COLUMN),
                GoCardlessResourceType.fromString(resultSet.getString(RESOURCE_TYPE_COLUMN)),
                resultSet.getString(JSON_COLUMN),
                resultSet.getString(DETAILS_CAUSE_COLUMN),
                resultSet.getString(DETAILS_DESCRIPTION_COLUMN),
                resultSet.getString(DETAILS_ORIGIN_COLUMN),
                resultSet.getString(DETAILS_REASON_CODE_COLUMN),
                resultSet.getString(DETAILS_SCHEME_COLUMN),
                resultSet.getString(LINKS_MANDATE_COLUMN),
                resultSet.getString(LINKS_NEW_CUSTOMER_BANK_ACCOUNT_COLUMN),
                resultSet.getString(LINKS_NEW_MANDATE_COLUMN),
                resultSet.getString(LINKS_ORGANISATION_COLUMN),
                resultSet.getString(LINKS_PARENT_EVENT_COLUMN),
                resultSet.getString(LINKS_PAYMENT_COLUMN),
                resultSet.getString(LINKS_PAYOUT_COLUMN),
                resultSet.getString(LINKS_PREVIOUS_CUSTOMER_BANK_ACCOUNT_COLUMN),
                resultSet.getString(LINKS_REFUND_COLUMN),
                resultSet.getString(LINKS_SUBSCRIPTION_COLUMN),
                ZonedDateTime.ofInstant(resultSet.getTimestamp(CREATED_AT_COLUMN).toInstant(), ZoneOffset.UTC));
    }
}
