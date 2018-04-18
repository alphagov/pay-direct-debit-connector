package uk.gov.pay.directdebit.payments.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.dao.mapper.TransactionMapper;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.util.List;
import java.util.Optional;

@RegisterMapper(TransactionMapper.class)
public interface TransactionDao {

    @SqlQuery("SELECT\n" +
            "  t.id AS transaction_id,\n" +
            "  t.payment_request_id AS transaction_payment_request_id,\n" +
            "  t.amount AS transaction_amount,\n" +
            "  t.type AS transaction_type,\n" +
            "  t.state AS transaction_state,\n" +
            "  p.id AS payment_request_id,\n" +
            "  p.external_id AS payment_request_external_id,\n" +
            "  p.gateway_account_id AS payment_request_gateway_account_id,\n" +
            "  p.amount AS payment_request_amount,\n" +
            "  p.reference AS payment_request_reference,\n" +
            "  p.description AS payment_request_description,\n" +
            "  p.return_url AS payment_request_return_url,\n" +
            "  p.created_date AS payment_request_created_date,\n" +
            "  g.id AS gateway_account_id,\n" +
            "  g.external_id AS gateway_account_external_id,\n" +
            "  g.payment_provider AS gateway_account_payment_provider,\n" +
            "  g.service_name AS gateway_account_service_name,\n" +
            "  g.type AS gateway_account_type,\n" +
            "  g.description AS gateway_account_description,\n" +
            "  g.analytics_id AS gateway_account_analytics_id\n" +
            "FROM transactions t JOIN payment_requests p ON p.id = t.payment_request_id\n" +
            "  JOIN gateway_accounts g ON p.gateway_account_id = g.id\n" +
            "WHERE t.id = :id")
    @SingleValueResult(Transaction.class)
    Optional<Transaction> findById(@Bind("id") Long id);

    @SqlQuery("SELECT\n" +
            "  t.id AS transaction_id,\n" +
            "  t.payment_request_id AS transaction_payment_request_id,\n" +
            "  t.amount AS transaction_amount,\n" +
            "  t.type AS transaction_type,\n" +
            "  t.state AS transaction_state,\n" +
            "  p.id AS payment_request_id,\n" +
            "  p.external_id AS payment_request_external_id,\n" +
            "  p.gateway_account_id AS payment_request_gateway_account_id,\n" +
            "  p.amount AS payment_request_amount,\n" +
            "  p.reference AS payment_request_reference,\n" +
            "  p.description AS payment_request_description,\n" +
            "  p.return_url AS payment_request_return_url,\n" +
            "  p.created_date AS payment_request_created_date,\n" +
            "  g.id AS gateway_account_id,\n" +
            "  g.external_id AS gateway_account_external_id,\n" +
            "  g.payment_provider AS gateway_account_payment_provider,\n" +
            "  g.service_name AS gateway_account_service_name,\n" +
            "  g.type AS gateway_account_type,\n" +
            "  g.description AS gateway_account_description,\n" +
            "  g.analytics_id AS gateway_account_analytics_id\n" +
            "FROM transactions t JOIN payment_requests p ON p.id = t.payment_request_id\n" +
            "  JOIN gateway_accounts g ON p.gateway_account_id = g.id\n" +
            "WHERE t.payment_request_id = :paymentRequestId")
    @SingleValueResult(Transaction.class)
    Optional<Transaction> findByPaymentRequestId(@Bind("paymentRequestId") Long paymentRequestId);

    @SqlQuery("SELECT\n" +
            "  t.id AS transaction_id,\n" +
            "  t.payment_request_id AS transaction_payment_request_id,\n" +
            "  t.amount AS transaction_amount,\n" +
            "  t.type AS transaction_type,\n" +
            "  t.state AS transaction_state,\n" +
            "  p.id AS payment_request_id,\n" +
            "  p.external_id AS payment_request_external_id,\n" +
            "  p.gateway_account_id AS payment_request_gateway_account_id,\n" +
            "  p.amount AS payment_request_amount,\n" +
            "  p.reference AS payment_request_reference,\n" +
            "  p.description AS payment_request_description,\n" +
            "  p.return_url AS payment_request_return_url,\n" +
            "  p.created_date AS payment_request_created_date,\n" +
            "  g.id AS gateway_account_id,\n" +
            "  g.external_id AS gateway_account_external_id,\n" +
            "  g.payment_provider AS gateway_account_payment_provider,\n" +
            "  g.service_name AS gateway_account_service_name,\n" +
            "  g.type AS gateway_account_type,\n" +
            "  g.description AS gateway_account_description,\n" +
            "  g.analytics_id AS gateway_account_analytics_id\n" +
            "FROM transactions t JOIN payment_requests p ON p.id = t.payment_request_id\n" +
            "  JOIN gateway_accounts g ON p.gateway_account_id = g.id\n" +
            "WHERE p.external_id = :paymentRequestExternalId AND g.external_id = :accountExternalId")
    @SingleValueResult(Transaction.class)
    Optional<Transaction> findTransactionForExternalIdAndGatewayAccountExternalId(@Bind("paymentRequestExternalId") String paymentRequestExternalId, @Bind("accountExternalId") String accountExternalId);

    @SqlQuery("SELECT\n" +
            "  tr.id AS transaction_id,\n" +
            "  tr.payment_request_id AS transaction_payment_request_id,\n" +
            "  tr.amount AS transaction_amount,\n" +
            "  tr.type AS transaction_type,\n" +
            "  tr.state AS transaction_state,\n" +
            "  p.id AS payment_request_id,\n" +
            "  p.external_id AS payment_request_external_id,\n" +
            "  p.gateway_account_id AS payment_request_gateway_account_id,\n" +
            "  p.amount AS payment_request_amount,\n" +
            "  p.reference AS payment_request_reference,\n" +
            "  p.description AS payment_request_description,\n" +
            "  p.return_url AS payment_request_return_url,\n" +
            "  p.created_date AS payment_request_created_date,\n" +
            "  g.id AS gateway_account_id,\n" +
            "  g.external_id AS gateway_account_external_id,\n" +
            "  g.payment_provider AS gateway_account_payment_provider,\n" +
            "  g.service_name AS gateway_account_service_name,\n" +
            "  g.type AS gateway_account_type,\n" +
            "  g.description AS gateway_account_description,\n" +
            "  g.analytics_id AS gateway_account_analytics_id\n" +
            "FROM transactions tr JOIN tokens t ON tr.payment_request_id = t.payment_request_id\n" +
            "  JOIN payment_requests p ON tr.payment_request_id = p.id\n" +
            "  JOIN gateway_accounts g ON p.gateway_account_id = g.id\n" +
            "WHERE t.secure_redirect_token = :tokenId")
    @SingleValueResult(Transaction.class)
    Optional<Transaction> findByTokenId(@Bind("tokenId") String tokenId);

    @SqlQuery("SELECT\n" +
            "  t.id AS transaction_id,\n" +
            "  t.payment_request_id AS transaction_payment_request_id,\n" +
            "  t.amount AS transaction_amount,\n" +
            "  t.type AS transaction_type,\n" +
            "  t.state AS transaction_state,\n" +
            "  p.id AS payment_request_id,\n" +
            "  p.external_id AS payment_request_external_id,\n" +
            "  p.gateway_account_id AS payment_request_gateway_account_id,\n" +
            "  p.amount AS payment_request_amount,\n" +
            "  p.reference AS payment_request_reference,\n" +
            "  p.description AS payment_request_description,\n" +
            "  p.return_url AS payment_request_return_url,\n" +
            "  p.created_date AS payment_request_created_date,\n" +
            "  g.id AS gateway_account_id,\n" +
            "  g.external_id AS gateway_account_external_id,\n" +
            "  g.payment_provider AS gateway_account_payment_provider,\n" +
            "  g.service_name AS gateway_account_service_name,\n" +
            "  g.type AS gateway_account_type,\n" +
            "  g.description AS gateway_account_description,\n" +
            "  g.analytics_id AS gateway_account_analytics_id\n" +
            "FROM transactions t JOIN payment_requests p ON p.id = t.payment_request_id\n" +
            "  JOIN gateway_accounts g ON p.gateway_account_id = g.id\n" +
            "WHERE t.state = :state AND g.payment_provider = :paymentProvider")
    List<Transaction> findAllByPaymentStateAndProvider(@Bind("state") PaymentState paymentState, @Bind("paymentProvider") PaymentProvider paymentProvider);

    @SqlQuery("SELECT\n" +
            "  t.id AS transaction_id,\n" +
            "  t.payment_request_id AS transaction_payment_request_id,\n" +
            "  t.amount AS transaction_amount,\n" +
            "  t.type AS transaction_type,\n" +
            "  t.state AS transaction_state,\n" +
            "  r.id AS payment_request_id,\n" +
            "  r.external_id AS payment_request_external_id,\n" +
            "  r.gateway_account_id AS payment_request_gateway_account_id,\n" +
            "  r.amount AS payment_request_amount,\n" +
            "  r.reference AS payment_request_reference,\n" +
            "  r.description AS payment_request_description,\n" +
            "  r.return_url AS payment_request_return_url,\n" +
            "  r.created_date AS payment_request_created_date,\n" +
            "  g.id AS gateway_account_id,\n" +
            "  g.external_id AS gateway_account_external_id,\n" +
            "  g.payment_provider AS gateway_account_payment_provider,\n" +
            "  g.service_name AS gateway_account_service_name,\n" +
            "  g.type AS gateway_account_type,\n" +
            "  g.description AS gateway_account_description,\n" +
            "  g.analytics_id AS gateway_account_analytics_id\n" +
            "FROM transactions t JOIN payment_requests r ON r.id = t.payment_request_id\n" +
            "  JOIN gateway_accounts g ON r.gateway_account_id = g.id\n" +
            "  JOIN payers p ON p.payment_request_id = t.payment_request_id\n" +
            "  JOIN mandates m ON m.payer_id = p.id\n" +
            "WHERE m.id = :mandateId")
    @SingleValueResult(Transaction.class)
    Optional<Transaction> findByMandateId(@Bind("mandateId") Long mandateId);

    @SqlUpdate("UPDATE transactions t SET state = :state WHERE t.id = :id")
    int updateState(@Bind("id") Long id, @Bind("state") PaymentState paymentState);

    @SqlUpdate("INSERT INTO transactions(payment_request_id, amount, type, state) VALUES (:paymentRequestId, :amount, :type, :state)")
    @GetGeneratedKeys
    Long insert(@BindBean Transaction transaction);

}
