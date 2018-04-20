package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payments.dao.mapper.PaymentRequestEventMapper;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;

import java.util.Optional;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type;

@RegisterRowMapper(PaymentRequestEventMapper.class)
public interface PaymentRequestEventDao {

    @SqlUpdate("INSERT INTO payment_request_events(payment_request_id, event_type, event, event_date) VALUES (:paymentRequestId, :eventType, :event, :eventDate)")
    @GetGeneratedKeys
    Long insert(@BindBean PaymentRequestEvent paymentRequestevent);

    @SqlQuery("SELECT * FROM payment_request_events e WHERE e.payment_request_id = :paymentRequestId and e.event_type = :eventType and e.event = :event")
    Optional<PaymentRequestEvent> findByPaymentRequestIdAndEvent(@Bind("paymentRequestId") Long paymentRequestId, @Bind("eventType") Type eventType, @Bind("event") SupportedEvent event);
}
