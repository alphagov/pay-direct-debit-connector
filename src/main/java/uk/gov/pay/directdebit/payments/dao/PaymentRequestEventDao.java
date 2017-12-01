package uk.gov.pay.directdebit.payments.dao;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import uk.gov.pay.directdebit.common.dao.DateArgumentFactory;
import uk.gov.pay.directdebit.payments.dao.mapper.PaymentRequestEventMapper;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;

@RegisterMapper(PaymentRequestEventMapper.class)
public interface PaymentRequestEventDao {

    @SqlUpdate("INSERT INTO payment_request_events(payment_request_id, event_type, event, event_date) VALUES (:paymentRequestId, :eventType, :event, :eventDate)")
    @GetGeneratedKeys
    @RegisterArgumentFactory(DateArgumentFactory.class)
    Long insert(@BindBean PaymentRequestEvent paymentRequestevent);
}
