package uk.gov.pay.directdebit.mandate.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.pay.directdebit.mandate.dao.mapper.GoCardlessPaymentMapper;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;

import java.util.Optional;

@RegisterMapper(GoCardlessPaymentMapper.class)
public interface GoCardlessPaymentDao {

    @SqlQuery("SELECT * FROM gocardless_payments p WHERE p.payment_id = :entityId" )
    @SingleValueResult(GoCardlessPayment.class)
    Optional<GoCardlessPayment> findByEventResourceId(@Bind("entityId") String entityId);

    @SqlUpdate("INSERT INTO gocardless_payments(transaction_id, payment_id) VALUES (:transactionId, :paymentId)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessPayment payment);
}
