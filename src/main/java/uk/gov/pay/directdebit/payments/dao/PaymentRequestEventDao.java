package uk.gov.pay.directdebit.payments.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.pay.directdebit.common.dao.DateArgumentFactory;
import uk.gov.pay.directdebit.payments.dao.mapper.PaymentRequestEventMapper;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;

import java.util.Optional;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.*;

@RegisterMapper(PaymentRequestEventMapper.class)
public interface PaymentRequestEventDao {

    @SqlUpdate("INSERT INTO payment_request_events(payment_request_id, event_type, event, event_date) VALUES (:paymentRequestId, :eventType, :event, :eventDate)")
    @GetGeneratedKeys
    @RegisterArgumentFactory(DateArgumentFactory.class)
    Long insert(@BindBean PaymentRequestEvent paymentRequestevent);

    @SqlQuery("SELECT * FROM payment_request_events e WHERE e.payment_request_id = :paymentRequestId and e.event_type = :eventType and e.event = :event")
    @SingleValueResult(PaymentRequestEvent.class)
    Optional<PaymentRequestEvent> findByPaymentRequestIdAndEvent(@Bind("paymentRequestId") Long paymentRequestId, @Bind("eventType") Type eventType, @Bind("event") SupportedEvent event);
}
