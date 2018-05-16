package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.dao.mapper.TransactionMapper;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.util.List;
import java.util.Optional;

@RegisterRowMapper(TransactionMapper.class)
public interface TransactionDao {

    String joinQuery = "SELECT" +
            "  t.id AS transaction_id," +
            "  t.payment_request_id AS transaction_payment_request_id," +
            "  t.amount AS transaction_amount," +
            "  t.type AS transaction_type," +
            "  t.state AS transaction_state," +
            "  p.id AS payment_request_id," +
            "  p.external_id AS payment_request_external_id," +
            "  p.gateway_account_id AS payment_request_gateway_account_id," +
            "  p.amount AS payment_request_amount," +
            "  p.reference AS payment_request_reference," +
            "  p.description AS payment_request_description," +
            "  p.return_url AS payment_request_return_url," +
            "  p.created_date AS payment_request_created_date," +
            "  g.id AS gateway_account_id," +
            "  g.external_id AS gateway_account_external_id," +
            "  g.payment_provider AS gateway_account_payment_provider," +
            "  g.service_name AS gateway_account_service_name," +
            "  g.type AS gateway_account_type," +
            "  g.description AS gateway_account_description," +
            "  g.analytics_id AS gateway_account_analytics_id," +
            "  y.id AS payer_id," +
            "  y.payment_request_id AS payer_payment_request_id," +
            "  y.external_id AS payer_external_id," +
            "  y.name AS payer_name," +
            "  y.email AS payer_email," +
            "  y.bank_account_number_last_two_digits AS payer_bank_account_number_last_two_digits," +
            "  y.bank_account_requires_authorisation AS payer_bank_account_requires_authorisation," +
            "  y.bank_account_number AS payer_bank_account_number," +
            "  y.bank_account_sort_code AS payer_bank_account_sort_code," +
            "  y.bank_name AS payer_bank_name," +
            "  y.created_date AS payer_created_date" +
            " FROM transactions t " + 
            "  JOIN payment_requests p ON t.payment_request_id = p.id" +
            "  JOIN gateway_accounts g ON p.gateway_account_id = g.id" +
            "  LEFT JOIN payers y ON y.payment_request_id = p.id";
    
    @SqlQuery(joinQuery + " WHERE t.id = :id")
    Optional<Transaction> findById(@Bind("id") Long id);

    @SqlQuery(joinQuery + " WHERE t.payment_request_id = :paymentRequestId")
    Optional<Transaction> findByPaymentRequestId(@Bind("paymentRequestId") Long paymentRequestId);

    @SqlQuery(joinQuery + " WHERE p.external_id = :paymentRequestExternalId AND g.external_id = :accountExternalId")
    Optional<Transaction> findTransactionForExternalIdAndGatewayAccountExternalId(@Bind("paymentRequestExternalId") String paymentRequestExternalId, @Bind("accountExternalId") String accountExternalId);

    @SqlQuery(joinQuery + " JOIN tokens k ON k.payment_request_id = t.payment_request_id WHERE k.secure_redirect_token = :tokenId")
    Optional<Transaction> findByTokenId(@Bind("tokenId") String tokenId);

    @SqlQuery(joinQuery + " WHERE t.state = :state AND g.payment_provider = :paymentProvider")
    List<Transaction> findAllByPaymentStateAndProvider(@Bind("state") PaymentState paymentState, @Bind("paymentProvider") PaymentProvider paymentProvider);

    @SqlQuery(joinQuery + " JOIN mandates m ON m.payer_id = y.id WHERE m.id = :mandateId")
    Optional<Transaction> findByMandateId(@Bind("mandateId") Long mandateId);

    @SqlUpdate("UPDATE transactions t SET state = :state WHERE t.id = :id")
    int updateState(@Bind("id") Long id, @Bind("state") PaymentState paymentState);

    @SqlUpdate("INSERT INTO transactions(payment_request_id, amount, type, state) VALUES (:paymentRequest.id, :amount, :type, :state)")
    @GetGeneratedKeys
    Long insert(@BindBean Transaction transaction);

}
