package uk.gov.pay.directdebit.payments.dao;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import uk.gov.pay.directdebit.payments.dao.mapper.TransactionMapper;
import uk.gov.pay.directdebit.payments.model.Transaction;

@RegisterMapper(TransactionMapper.class)
public interface TransactionDao {
    @SqlUpdate("INSERT INTO transactions(payment_request_id, amount, type, state) VALUES (:paymentRequestId, :amount, :type, :state)")
    @GetGeneratedKeys
    Long insert(@BindBean Transaction transaction);
}
