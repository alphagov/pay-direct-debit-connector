package uk.gov.pay.directdebit.payments.dao;

import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.pay.directdebit.common.dao.DateArgumentFactory;
import uk.gov.pay.directdebit.payments.dao.mapper.PaymentRequestMapper;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;

import java.util.Optional;

@RegisterMapper(PaymentRequestMapper.class)
public interface PaymentRequestDao {
    @SqlQuery("SELECT * FROM payment_requests p WHERE p.id = :id")
    @SingleValueResult(PaymentRequest.class)
    Optional<PaymentRequest> findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM payment_requests p WHERE p.external_id = :externalId")
    @SingleValueResult(PaymentRequest.class)
    Optional<PaymentRequest> findByExternalId(@Bind("externalId") String externalId);

    @SqlUpdate("INSERT INTO payment_requests(external_id, gateway_account_id, amount, reference, description, return_url, created_date) VALUES (:externalId, :gatewayAccountId, :amount, :reference, :description, :returnUrl, :createdDate)")
    @GetGeneratedKeys
    @RegisterArgumentFactory(DateArgumentFactory.class)
    Long insert(@BindBean PaymentRequest paymentRequest);
}
