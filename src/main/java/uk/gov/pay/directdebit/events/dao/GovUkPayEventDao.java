package uk.gov.pay.directdebit.events.dao;

import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;

public interface GovUkPayEventDao {
    
    @SqlUpdate("INSERT INTO govukpay_events(mandate_id," +
            " payment_id, event_date, resource_type, event_type) VALUES (:mandateId, :paymentId, :eventDate," +
            " :resourceType, :eventType)")
    @GetGeneratedKeys
    Long insert(@BindBean GovUkPayEvent govUkPayEvent);
}
