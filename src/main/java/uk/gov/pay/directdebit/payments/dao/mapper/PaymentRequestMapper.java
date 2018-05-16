package uk.gov.pay.directdebit.payments.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class PaymentRequestMapper implements RowMapper<PaymentRequest> {
    private static final String ID_COLUMN = "payment_request_id";
    private static final String EXTERNAL_ID_COLUMN = "payment_request_external_id";
    private static final String GATEWAY_ACCOUNT_ID_COLUMN = "payment_request_gateway_account_id";
    private static final String AMOUNT_COLUMN = "payment_request_amount";
    private static final String REFERENCE_COLUMN = "payment_request_reference";
    private static final String DESCRIPTION_COLUMN = "payment_request_description";
    private static final String RETURN_URL_COLUMN = "payment_request_return_url";
    private static final String CREATED_DATE_COLUMN = "payment_request_created_date";
    private static final String PAYER_ID_COLUMN = "payer_id";
    private static final String PAYER_PAYMENT_REQUEST_ID_COLUMN = "payer_payment_request_id";
    private static final String PAYER_EXTERNAL_ID_COLUMN = "payer_external_id";
    private static final String PAYER_NAME_COLUMN = "payer_name";
    private static final String PAYER_EMAIL_COLUMN = "payer_email";
    private static final String PAYER_BANK_ACCOUNT_NUMBER_LAST_TWO_DIGITS_COLUMN = "payer_bank_account_number_last_two_digits";
    private static final String PAYER_BANK_ACCOUNT_REQUIRES_AUTHORISATION_COLUMN = "payer_bank_account_requires_authorisation";
    private static final String PAYER_BANK_ACCOUNT_NUMBER_COLUMN = "payer_bank_account_number";
    private static final String PAYER_BANK_ACCOUNT_SORT_CODE_COLUMN = "payer_bank_account_sort_code";
    private static final String PAYER_BANK_NAME_COLUMN = "payer_bank_name";
    private static final String PAYER_CREATED_DATE_COLUMN= "payer_created_date";

    @Override
    public PaymentRequest map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        Payer payer = null;
        if (resultSet.getTimestamp(PAYER_CREATED_DATE_COLUMN) != null) {
            payer = new Payer(
                    resultSet.getLong(PAYER_ID_COLUMN),
                    resultSet.getLong(PAYER_PAYMENT_REQUEST_ID_COLUMN),
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
        return new PaymentRequest(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(AMOUNT_COLUMN),
                resultSet.getString(RETURN_URL_COLUMN),
                resultSet.getLong(GATEWAY_ACCOUNT_ID_COLUMN),
                resultSet.getString(DESCRIPTION_COLUMN),
                resultSet.getString(REFERENCE_COLUMN),
                resultSet.getString(EXTERNAL_ID_COLUMN),
                payer,
                ZonedDateTime.ofInstant(resultSet.getTimestamp(CREATED_DATE_COLUMN).toInstant(), ZoneOffset.UTC));
    }
}
