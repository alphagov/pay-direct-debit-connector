package uk.gov.pay.directdebit.mandate.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.mandate.dao.mapper.GoCardlessPaymentMapper;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;

import java.util.Optional;

@RegisterRowMapper(GoCardlessPaymentMapper.class)
public interface GoCardlessPaymentDao {

    @SqlQuery("SELECT * FROM gocardless_payments p WHERE p.payment_id = :entityId")
    Optional<GoCardlessPayment> findByEventResourceId(@Bind("entityId") String entityId);

    @SqlUpdate("INSERT INTO gocardless_payments(transaction_id, payment_id) VALUES (:transactionId, :paymentId)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessPayment payment);
    
    @SqlUpdate("UPDATE gocardless_payments SET payment_id = :paymentId WHERE id = :id")
    Long update(@BindBean GoCardlessPayment payment);
}
