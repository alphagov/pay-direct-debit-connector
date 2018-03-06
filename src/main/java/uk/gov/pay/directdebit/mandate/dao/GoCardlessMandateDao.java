package uk.gov.pay.directdebit.mandate.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.pay.directdebit.mandate.dao.mapper.GoCardlessMandateMapper;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;

import java.util.Optional;

@RegisterMapper(GoCardlessMandateMapper.class)
public interface GoCardlessMandateDao {

    @SqlQuery("SELECT * FROM gocardless_mandates m WHERE m.gocardless_mandate_id = :entityId" )
    @SingleValueResult(GoCardlessMandate.class)
    Optional<GoCardlessMandate> findByEventResourceId(@Bind("entityId") String entityId);

    @SqlUpdate("INSERT INTO gocardless_mandates(mandate_id, gocardless_mandate_id) VALUES (:mandateId, :goCardlessMandateId)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessMandate mandate);
}
