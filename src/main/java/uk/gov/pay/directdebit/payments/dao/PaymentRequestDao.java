package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payments.dao.mapper.PaymentRequestMapper;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;

import java.util.Optional;

@RegisterRowMapper(PaymentRequestMapper.class)
public interface PaymentRequestDao {
    @SqlQuery("SELECT * FROM payment_requests p WHERE p.id = :id")
    Optional<PaymentRequest> findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM payment_requests p JOIN gateway_accounts g ON p.gateway_account_id = g.id WHERE p.external_id = :externalId AND g.external_id = :accountExternalId")
    Optional<PaymentRequest> findByExternalIdAndAccountExternalId(@Bind("externalId") String externalId, @Bind("accountExternalId") String accountExternalId);

    @SqlUpdate("INSERT INTO payment_requests(external_id, gateway_account_id, amount, reference, description, return_url, created_date) VALUES (:externalId, :gatewayAccountId, :amount, :reference, :description, :returnUrl, :createdDate)")
    @GetGeneratedKeys
    Long insert(@BindBean PaymentRequest paymentRequest);
}
