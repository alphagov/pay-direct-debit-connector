package uk.gov.pay.directdebit.mandate.dao;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import uk.gov.pay.directdebit.mandate.dao.mapper.GoCardlessMandateMapper;
import uk.gov.pay.directdebit.mandate.dao.mapper.MandateMapper;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.Mandate;

@RegisterMapper(GoCardlessMandateMapper.class)
public interface GoCardlessMandateDao {

    @SqlUpdate("INSERT INTO gocardless_mandates(mandate_id, gocardless_mandate_id) VALUES (:mandateId, :goCardlessMandateId)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessMandate mandate);
}
