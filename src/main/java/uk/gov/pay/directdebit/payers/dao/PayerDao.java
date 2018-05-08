package uk.gov.pay.directdebit.payers.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payers.dao.mapper.PayerMapper;
import uk.gov.pay.directdebit.payers.model.Payer;

import java.util.Map;
import java.util.Optional;

@RegisterRowMapper(PayerMapper.class)
public interface PayerDao {
    @SqlQuery("SELECT * FROM payers p WHERE p.id = :id")
    Optional<Payer> findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM payers p WHERE p.external_id = :externalId")
    Optional<Payer> findByExternalId(@Bind("externalId") String externalId);

    @SqlQuery("SELECT * FROM payers p WHERE p.payment_request_id = :paymentRequestId LIMIT 1")
    Optional<Payer> findByPaymentRequestId(@Bind("paymentRequestId") Long paymentRequestId);

    @SqlUpdate("INSERT INTO payers(payment_request_id, external_id, name, email, bank_account_number_last_two_digits, bank_account_requires_authorisation, bank_account_number, bank_account_sort_code, address_line1, address_line2, address_postcode, address_city, address_country, created_date ) VALUES (:paymentRequestId, :externalId, :name, :email, :accountNumberLastTwoDigits, :accountRequiresAuthorisation, :accountNumber, :sortCode, :addressLine1, :addressLine2, :addressPostcode, :addressCity, :addressCountry, :createdDate)")
    @GetGeneratedKeys
    Long insert(@BindBean Payer payer);

    @SqlUpdate("UPDATE payers p SET name = :name,  email = :email, bank_account_number_last_two_digits = :accountNumberLastTwoDigits, bank_account_requires_authorisation = :accountRequiresAuthorisation, bank_account_number = :accountNumber, bank_account_sort_code = :sortCode, address_line1 = :addressLine1, address_line2 = :addressLine2, address_postcode = :addressPostcode, address_city = :addressCity, address_country = :addressCountry WHERE p.id = :id")
    int updatePayerDetails(@Bind("id")Long id, @BindBean Payer payer);
}
