package uk.gov.pay.directdebit.mandate.dao;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import uk.gov.pay.directdebit.mandate.dao.mapper.GoCardlessPaymentMapper;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;

@RegisterMapper(GoCardlessPaymentMapper.class)
public interface GoCardlessPaymentDao {

    @SqlUpdate("INSERT INTO gocardless_payments(transaction_id, payment_id) VALUES (:transactionId, :paymentId)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessPayment payment);
}
