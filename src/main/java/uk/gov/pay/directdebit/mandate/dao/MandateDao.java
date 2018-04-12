package uk.gov.pay.directdebit.mandate.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.pay.directdebit.mandate.dao.mapper.MandateMapper;
import uk.gov.pay.directdebit.mandate.model.Mandate;

import java.util.Optional;

@RegisterMapper(MandateMapper.class)
public interface MandateDao {

    @SqlUpdate("INSERT INTO mandates(payer_id, external_id) VALUES (:payerId, :externalId)")
    @GetGeneratedKeys
    Long insert(@BindBean Mandate mandate);

    @SqlQuery("SELECT * FROM mandates m JOIN payers p ON m.payer_id = p.id JOIN transactions t ON t.payment_request_id = p.payment_request_id WHERE t.id = :transactionId")
    @SingleValueResult(Mandate.class)
    Optional<Mandate> findByTransactionId(@Bind("transactionId") Long transactionId);
}
