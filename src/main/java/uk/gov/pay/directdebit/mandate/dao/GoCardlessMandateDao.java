package uk.gov.pay.directdebit.mandate.dao;

import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.common.model.subtype.gocardless.creditor.GoCardlessCreditorIdArgumentFactory;
import uk.gov.pay.directdebit.mandate.dao.mapper.GoCardlessMandateMapper;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;

import java.util.Optional;

@RegisterArgumentFactory(GoCardlessCreditorIdArgumentFactory.class)
@RegisterRowMapper(GoCardlessMandateMapper.class)
public interface GoCardlessMandateDao {

    @SqlQuery("SELECT * FROM gocardless_mandates m WHERE m.gocardless_mandate_id = :entityId")
    Optional<GoCardlessMandate> findByEventResourceId(@Bind("entityId") String entityId);

    @SqlQuery("SELECT * FROM gocardless_mandates m WHERE m.mandate_id = :mandateId")
    Optional<GoCardlessMandate> findByMandateId(@Bind("mandateId") Long mandateId);

    @SqlUpdate("INSERT INTO gocardless_mandates(mandate_id, gocardless_mandate_id, gocardless_creditor_id) VALUES (:mandateId, :goCardlessMandateId, :goCardlessCreditorId)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessMandate mandate);
}
