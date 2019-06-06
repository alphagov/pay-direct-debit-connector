package uk.gov.pay.directdebit.payments.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.mandate.model.Mandate.MandateBuilder.aMandate;

public class TransactionMapper implements RowMapper<Transaction> {

    private static final String TRANSACTION_ID_COLUMN = "transaction_id";
    private static final String TRANSACTION_AMOUNT_COLUMN = "transaction_amount";
    private static final String TRANSACTION_STATE_COLUMN = "transaction_state";
    private static final String TRANSACTION_EXTERNAL_ID_COLUMN = "transaction_external_id";
    private static final String TRANSACTION_REFERENCE_COLUMN = "transaction_reference";
    private static final String TRANSACTION_DESCRIPTION_COLUMN = "transaction_description";
    private static final String TRANSACTION_CREATED_DATE_COLUMN = "transaction_created_date";
    private static final String GATEWAY_ACCOUNT_ID_COLUMN = "gateway_account_id";
    private static final String GATEWAY_ACCOUNT_EXTERNAL_ID_COLUMN = "gateway_account_external_id";
    private static final String GATEWAY_ACCOUNT_PAYMENT_PROVIDER_COLUMN = "gateway_account_payment_provider";
    private static final String GATEWAY_ACCOUNT_TYPE_COLUMN = "gateway_account_type";
    private static final String GATEWAY_ACCOUNT_DESCRIPTION_COLUMN = "gateway_account_description";
    private static final String GATEWAY_ACCOUNT_ANALYTICS_ID_COLUMN = "gateway_account_analytics_id";
    private static final String GATEWAY_ACCOUNT_ACCESS_TOKEN_COLUMN = "gateway_account_access_token";
    private static final String GATEWAY_ACCOUNT_ORGANISATION_COLUMN = "gateway_account_organisation";
    private static final String PAYER_ID_COLUMN = "payer_id";
    private static final String PAYER_MANDATE_ID_COLUMN = "payer_mandate_id";
    private static final String PAYER_EXTERNAL_ID_COLUMN = "payer_external_id";
    private static final String PAYER_NAME_COLUMN = "payer_name";
    private static final String PAYER_EMAIL_COLUMN = "payer_email";
    private static final String PAYER_BANK_ACCOUNT_NUMBER_LAST_TWO_DIGITS_COLUMN = "payer_bank_account_number_last_two_digits";
    private static final String PAYER_BANK_ACCOUNT_REQUIRES_AUTHORISATION_COLUMN = "payer_bank_account_requires_authorisation";
    private static final String PAYER_BANK_ACCOUNT_NUMBER_COLUMN = "payer_bank_account_number";
    private static final String PAYER_BANK_ACCOUNT_SORT_CODE_COLUMN = "payer_bank_account_sort_code";
    private static final String PAYER_BANK_NAME_COLUMN = "payer_bank_name";
    private static final String PAYER_CREATED_DATE_COLUMN = "payer_created_date";
    private static final String MANDATE_ID_COLUMN = "mandate_id";
    private static final String MANDATE_EXTERNAL_ID_COLUMN = "mandate_external_id";
    private static final String MANDATE_RETURN_URL_COLUMN = "mandate_return_url";
    private static final String MANDATE_STATE_COLUMN = "mandate_state";
    private static final String MANDATE_TYPE_COLUMN = "mandate_type";
    private static final String MANDATE_MANDATE_REFERENCE_COLUMN = "mandate_mandate_reference";
    private static final String MANDATE_SERVICE_REFERENCE_COLUMN = "mandate_service_reference";
    private static final String MANDATE_CREATED_DATE_COLUMN = "mandate_created_date";

    @Override
    public Transaction map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        Payer payer = null;
        if (resultSet.getTimestamp(PAYER_CREATED_DATE_COLUMN) != null) {
            payer = new Payer(
                    resultSet.getLong(PAYER_ID_COLUMN),
                    resultSet.getLong(PAYER_MANDATE_ID_COLUMN),
                    resultSet.getString(PAYER_EXTERNAL_ID_COLUMN),
                    resultSet.getString(PAYER_NAME_COLUMN),
                    resultSet.getString(PAYER_EMAIL_COLUMN),
                    resultSet.getString(PAYER_BANK_ACCOUNT_SORT_CODE_COLUMN),
                    resultSet.getString(PAYER_BANK_ACCOUNT_NUMBER_COLUMN),
                    resultSet.getString(PAYER_BANK_ACCOUNT_NUMBER_LAST_TWO_DIGITS_COLUMN),
                    resultSet.getBoolean(PAYER_BANK_ACCOUNT_REQUIRES_AUTHORISATION_COLUMN),
                    resultSet.getString(PAYER_BANK_NAME_COLUMN),
                    ZonedDateTime.ofInstant(resultSet.getTimestamp(PAYER_CREATED_DATE_COLUMN).toInstant(), ZoneOffset.UTC));
        }

        GatewayAccount gatewayAccount = new GatewayAccount(
                resultSet.getLong(GATEWAY_ACCOUNT_ID_COLUMN),
                resultSet.getString(GATEWAY_ACCOUNT_EXTERNAL_ID_COLUMN),
                PaymentProvider.fromString(resultSet.getString(GATEWAY_ACCOUNT_PAYMENT_PROVIDER_COLUMN)),
                GatewayAccount.Type.fromString(resultSet.getString(GATEWAY_ACCOUNT_TYPE_COLUMN)),
                resultSet.getString(GATEWAY_ACCOUNT_DESCRIPTION_COLUMN),
                resultSet.getString(GATEWAY_ACCOUNT_ANALYTICS_ID_COLUMN));
        String accessToken = resultSet.getString(GATEWAY_ACCOUNT_ACCESS_TOKEN_COLUMN);
        if (accessToken != null) {
            gatewayAccount.setAccessToken(PaymentProviderAccessToken.of(accessToken));
        }
        String organisation = resultSet.getString(GATEWAY_ACCOUNT_ORGANISATION_COLUMN);
        if (organisation != null) {
            gatewayAccount.setOrganisation(GoCardlessOrganisationId.valueOf(organisation));
        }

        Mandate mandate = aMandate()
                .withId(resultSet.getLong(MANDATE_ID_COLUMN))
                .withGatewayAccount(gatewayAccount)
                .withExternalId(MandateExternalId.valueOf(resultSet.getString(MANDATE_EXTERNAL_ID_COLUMN)))
                .withMandateReference(MandateBankStatementReference.valueOf(resultSet.getString(MANDATE_MANDATE_REFERENCE_COLUMN)))
                .withServiceReference(resultSet.getString(MANDATE_SERVICE_REFERENCE_COLUMN))
                .withState(MandateState.valueOf(resultSet.getString(MANDATE_STATE_COLUMN)))
                .withReturnUrl(resultSet.getString(MANDATE_RETURN_URL_COLUMN))
                .withCreatedDate(ZonedDateTime.ofInstant(resultSet.getTimestamp(MANDATE_CREATED_DATE_COLUMN).toInstant(), ZoneOffset.UTC))
                .withPayer(payer)
                .build();

        return new Transaction(
                resultSet.getLong(TRANSACTION_ID_COLUMN),
                resultSet.getString(TRANSACTION_EXTERNAL_ID_COLUMN),
                resultSet.getLong(TRANSACTION_AMOUNT_COLUMN),
                PaymentState.valueOf(resultSet.getString(TRANSACTION_STATE_COLUMN)),
                resultSet.getString(TRANSACTION_DESCRIPTION_COLUMN),
                resultSet.getString(TRANSACTION_REFERENCE_COLUMN),
                mandate,
                ZonedDateTime.ofInstant(resultSet.getTimestamp(TRANSACTION_CREATED_DATE_COLUMN).toInstant(), ZoneOffset.UTC));
    }
}
