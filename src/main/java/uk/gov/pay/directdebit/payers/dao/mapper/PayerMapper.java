package uk.gov.pay.directdebit.payers.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payers.model.Payer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class PayerMapper implements RowMapper<Payer> {
    private static final String ID_COLUMN = "id";
    private static final String PAYMENT_REQUEST_ID_COLUMN = "payment_request_id";
    private static final String EXTERNAL_ID_COLUMN = "external_id";
    private static final String NAME_COLUMN = "name";
    private static final String EMAIL_COLUMN = "email";
    private static final String BANK_ACCOUNT_LAST_DIGITS_COLUMN = "bank_account_number_last_two_digits";
    private static final String BANK_ACCOUNT_REQUIRES_AUTH_COLUMN = "bank_account_requires_authorisation";
    private static final String BANK_ACCOUNT_NUMBER_COLUMN = "bank_account_number";
    private static final String BANK_ACCOUNT_SORT_CODE_COLUMN = "bank_account_sort_code";
    private static final String ADDRESS_LINE1_COLUMN = "address_line1";
    private static final String ADDRESS_LINE2_COLUMN = "address_line2";
    private static final String ADDRESS_POSTCODE_COLUMN = "address_postcode";
    private static final String ADDRESS_CITY_COLUMN = "address_city";
    private static final String ADDRESS_COUNTRY_COLUMN = "address_country";
    private static final String CREATED_DATE_COLUMN = "created_date";

    @Override
    public Payer map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new Payer(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(PAYMENT_REQUEST_ID_COLUMN),
                resultSet.getString(EXTERNAL_ID_COLUMN),
                resultSet.getString(NAME_COLUMN),
                resultSet.getString(EMAIL_COLUMN),
                resultSet.getString(BANK_ACCOUNT_SORT_CODE_COLUMN),
                resultSet.getString(BANK_ACCOUNT_NUMBER_COLUMN),
                resultSet.getString(BANK_ACCOUNT_LAST_DIGITS_COLUMN),
                resultSet.getBoolean(BANK_ACCOUNT_REQUIRES_AUTH_COLUMN),
                resultSet.getString(ADDRESS_LINE1_COLUMN),
                resultSet.getString(ADDRESS_LINE2_COLUMN),
                resultSet.getString(ADDRESS_POSTCODE_COLUMN),
                resultSet.getString(ADDRESS_CITY_COLUMN),
                resultSet.getString(ADDRESS_COUNTRY_COLUMN),
                ZonedDateTime.ofInstant(resultSet.getTimestamp(CREATED_DATE_COLUMN).toInstant(), ZoneOffset.UTC));
    }
}
