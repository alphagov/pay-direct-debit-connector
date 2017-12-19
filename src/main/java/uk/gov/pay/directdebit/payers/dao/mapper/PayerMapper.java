package uk.gov.pay.directdebit.payers.dao.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PayerMapper implements ResultSetMapper<Payer> {
    private static final String ID_COLUMN = "id";
    private static final String PAYMENT_REQUEST_ID_COLUMN = "payment_request_id";
    private static final String EXTERNAL_ID_COLUMN = "external_id";
    private static final String NAME_COLUMN = "name";
    private static final String EMAIL_COLUMN = "email";
    private static final String BANK_ACCOUNT_LAST_DIGITS_COLUMN = "bank_account_number_last_two_digits";
    private static final String BANK_ACCOUNT_REQUIRES_AUTH_COLUMN = "bank_account_requires_authorisation";
    private static final String BANK_ACCOUNT_NUMBER = "bank_account_number";
    private static final String BANK_ACCOUNT_SORT_CODE = "bank_account_sort_code";
    private static final String ADDRESS_LINE1 = "address_line1";
    private static final String ADDRESS_LINE2 = "address_line2";
    private static final String ADDRESS_POSTCODE = "address_postcode";
    private static final String ADDRESS_CITY = "address_city";
    private static final String ADDRESS_COUNTRY = "address_country";
    private static final String CREATED_DATE_COLUMN = "created_date";

    @Override
    public Payer map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new Payer(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(PAYMENT_REQUEST_ID_COLUMN),
                resultSet.getString(EXTERNAL_ID_COLUMN),
                resultSet.getString(NAME_COLUMN),
                resultSet.getString(EMAIL_COLUMN),
                resultSet.getString(BANK_ACCOUNT_SORT_CODE),
                resultSet.getString(BANK_ACCOUNT_NUMBER),
                resultSet.getString(BANK_ACCOUNT_LAST_DIGITS_COLUMN),
                resultSet.getBoolean(BANK_ACCOUNT_REQUIRES_AUTH_COLUMN),
                resultSet.getString(ADDRESS_LINE1),
                resultSet.getString(ADDRESS_LINE2),
                resultSet.getString(ADDRESS_POSTCODE),
                resultSet.getString(ADDRESS_CITY),
                resultSet.getString(ADDRESS_COUNTRY),
                ZonedDateTime.ofInstant(resultSet.getTimestamp(CREATED_DATE_COLUMN).toInstant(), ZoneId.of("UTC")));
    }
}
