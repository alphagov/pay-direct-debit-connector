package uk.gov.pay.directdebit.payments.dao;

import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.pay.directdebit.payments.dao.mapper.TransactionMapper;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.util.Optional;

@RegisterMapper(TransactionMapper.class)
public interface TransactionDao {
    @SqlQuery("SELECT * FROM transactions t INNER JOIN payment_requests p ON p.id = t.payment_request_id WHERE t.payment_request_id = :paymentRequestId")
    @SingleValueResult(Transaction.class)
    Optional<Transaction> findByPaymentRequestId(@Bind("paymentRequestId") Long paymentRequestId);

    @SqlUpdate("INSERT INTO transactions(payment_request_id, amount, type, state) VALUES (:paymentRequestId, :amount, :type, :state)")
    @GetGeneratedKeys
    Long insert(@BindBean Transaction transaction);


}
