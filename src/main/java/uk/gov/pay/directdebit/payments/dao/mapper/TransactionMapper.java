package uk.gov.pay.directdebit.payments.dao.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TransactionMapper implements ResultSetMapper<Transaction> {

    private static final String TRANSACTION_ID_COLUMN = "transaction_id";
    private static final String TRANSACTION_AMOUNT_COLUMN = "transaction_amount";
    private static final String TRANSACTION_TYPE_COLUMN = "transaction_type";
    private static final String TRANSACTION_STATE_COLUMN = "transaction_state";
    private static final String PAYMENT_REQUEST_ID_COLUMN = "payment_request_id";
    private static final String PAYMENT_REQUEST_EXTERNAL_ID_COLUMN = "payment_request_external_id";
    private static final String PAYMENT_REQUEST_REFERENCE_COLUMN = "payment_request_reference";
    private static final String PAYMENT_REQUEST_RETURN_URL_COLUMN = "payment_request_return_url";
    private static final String PAYMENT_REQUEST_DESCRIPTION_COLUMN = "payment_request_description";
    private static final String GATEWAY_ACCOUNT_ID_COLUMN = "gateway_account_id";
    private static final String GATEWAY_ACCOUNT_EXTERNAL_ID_COLUMN = "gateway_account_external_id";
    private static final String GATEWAY_ACCOUNT_PAYMENT_PROVIDER_COLUMN = "gateway_account_payment_provider";

    @Override
    public Transaction map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        PaymentRequest paymentRequest = new PaymentRequest(
                resultSet.getLong(PAYMENT_REQUEST_ID_COLUMN),
                resultSet.getLong(TRANSACTION_AMOUNT_COLUMN),
                resultSet.getString(PAYMENT_REQUEST_RETURN_URL_COLUMN),
                resultSet.getLong(GATEWAY_ACCOUNT_ID_COLUMN),
                resultSet.getString(PAYMENT_REQUEST_DESCRIPTION_COLUMN),
                resultSet.getString(PAYMENT_REQUEST_REFERENCE_COLUMN),
                resultSet.getString(PAYMENT_REQUEST_EXTERNAL_ID_COLUMN),
                ZonedDateTime.now(ZoneOffset.UTC)
        );
        GatewayAccount gatewayAccount = new GatewayAccount(
                resultSet.getLong(GATEWAY_ACCOUNT_ID_COLUMN),
                resultSet.getString(GATEWAY_ACCOUNT_EXTERNAL_ID_COLUMN),
                PaymentProvider.fromString(resultSet.getString(GATEWAY_ACCOUNT_PAYMENT_PROVIDER_COLUMN)),
                null, null, null, null
        );

        return new Transaction(
                resultSet.getLong(TRANSACTION_ID_COLUMN),
                paymentRequest,
                gatewayAccount,
                Transaction.Type.valueOf(resultSet.getString(TRANSACTION_TYPE_COLUMN)),
                PaymentState.valueOf(resultSet.getString(TRANSACTION_STATE_COLUMN))
        );
    }

}
