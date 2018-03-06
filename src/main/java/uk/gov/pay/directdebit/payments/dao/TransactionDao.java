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


    @SqlQuery("SELECT * FROM transactions t JOIN payment_requests p ON p.id = t.payment_request_id JOIN gateway_accounts g ON p.gateway_account_id = g.id WHERE t.id = :id")
    @SingleValueResult(Transaction.class)
    Optional<Transaction> findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM transactions t JOIN payment_requests p ON p.id = t.payment_request_id JOIN gateway_accounts g ON p.gateway_account_id = g.id WHERE t.payment_request_id = :paymentRequestId")
    @SingleValueResult(Transaction.class)
    Optional<Transaction> findByPaymentRequestId(@Bind("paymentRequestId") Long paymentRequestId);

    @SqlQuery("SELECT * FROM transactions t JOIN payment_requests p ON p.id = t.payment_request_id JOIN gateway_accounts g ON p.gateway_account_id = g.id WHERE p.external_id = :paymentRequestExternalId AND p.gateway_account_id = :accountId")
    @SingleValueResult(Transaction.class)
    Optional<Transaction> findByPaymentRequestExternalIdAndAccountId(@Bind("paymentRequestExternalId") String paymentRequestExternalId, @Bind("accountId") Long accountId);

    @SqlQuery("SELECT * FROM transactions tr JOIN tokens t ON tr.payment_request_id = t.payment_request_id JOIN payment_requests p ON tr.payment_request_id = p.id JOIN gateway_accounts g ON p.gateway_account_id = g.id WHERE t.secure_redirect_token = :tokenId")
    @SingleValueResult(Transaction.class)
    Optional<Transaction> findByTokenId(@Bind("tokenId") String tokenId);

    @SqlQuery("SELECT * FROM transactions t JOIN payment_requests p ON p.id = t.payment_request_id JOIN gateway_accounts g ON p.gateway_account_id = g.id WHERE t.state = :state AND g.payment_provider = :paymentProvider")
    List<Transaction> findAllByPaymentStateAndProvider(@Bind("state") PaymentState paymentState, @Bind("paymentProvider") PaymentProvider paymentProvider);

    @SqlQuery("SELECT * FROM transactions t JOIN payers p ON p.payment_request_id = t.payment_request_id JOIN mandates m ON m.payer_id = p.id WHERE m.id = :mandateId")
    @SingleValueResult(Transaction.class)
    Optional<Transaction> findByMandateId(@Bind("mandateId") Long mandateId);


    @SqlUpdate("UPDATE transactions t SET state = :state WHERE t.id = :id")
    int updateState(@Bind("id") Long id, @Bind("state") PaymentState paymentState);

    @SqlUpdate("INSERT INTO transactions(payment_request_id, amount, type, state) VALUES (:paymentRequestId, :amount, :type, :state)")
    @GetGeneratedKeys
    Long insert(@BindBean Transaction transaction);

}
