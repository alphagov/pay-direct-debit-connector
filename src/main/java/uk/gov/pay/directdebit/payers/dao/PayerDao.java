package uk.gov.pay.directdebit.payers.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.pay.directdebit.common.dao.DateArgumentFactory;
import uk.gov.pay.directdebit.payers.dao.mapper.PayerMapper;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.dao.mapper.PaymentRequestMapper;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;

import java.util.Optional;

@RegisterMapper(PayerMapper.class)
public interface PayerDao {
    @SqlQuery("SELECT * FROM payers p WHERE p.id = :id")
    @SingleValueResult(Payer.class)
    Optional<Payer> findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM payers p WHERE p.external_id = :externalId")
    @SingleValueResult(Payer.class)
    Optional<Payer> findByExternalId(@Bind("externalId") String externalId);

    @SqlQuery("SELECT * FROM payers p WHERE p.payment_request_id = :paymentRequestId")
    @SingleValueResult(Payer.class)
    Optional<Payer> findByPaymentRequestId(@Bind("paymentRequestId") Long paymentRequestId);

    @SqlUpdate("INSERT INTO payers(payment_request_id, external_id, name, email, bank_account_number_last_two_digits, bank_account_requires_authorisation, bank_account_number, bank_account_sort_code, address_line1, address_line2, address_postcode, address_city, address_country, created_date ) VALUES (:paymentRequestId, :externalId, :name, :email, :accountNumberLastTwoDigits, :accountRequiresAuthorisation, :accountNumber, :sortCode, :addressLine1, :addressLine2, :addressPostcode, :addressCity, :addressCountry, :createdDate)")
    @GetGeneratedKeys
    @RegisterArgumentFactory(DateArgumentFactory.class)
    Long insert(@BindBean Payer payer);
}
