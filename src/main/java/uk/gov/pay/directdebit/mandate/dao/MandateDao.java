package uk.gov.pay.directdebit.mandate.dao;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import uk.gov.pay.directdebit.mandate.dao.mapper.MandateMapper;
import uk.gov.pay.directdebit.mandate.model.Mandate;

@RegisterMapper(MandateMapper.class)
public interface MandateDao {

    @SqlUpdate("INSERT INTO mandates(payer_id, external_id) VALUES (:payerId, :externalId)")
    @GetGeneratedKeys
    Long insert(@BindBean Mandate mandate);
}
