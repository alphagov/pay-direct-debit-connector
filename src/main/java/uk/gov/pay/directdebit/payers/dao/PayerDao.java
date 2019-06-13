package uk.gov.pay.directdebit.payers.dao;

import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payers.dao.mapper.PayerMapper;
import uk.gov.pay.directdebit.payers.model.Payer;

@RegisterRowMapper(PayerMapper.class)
public interface PayerDao {
    @SqlQuery("SELECT * FROM payers p WHERE p.external_id = :externalId")
    Optional<Payer> findByExternalId(@Bind("externalId") String externalId);

    @SqlQuery("SELECT * FROM payers p JOIN mandates m ON p.mandate_id = m.id  JOIN payments payments ON payments.mandate_id = m.id WHERE payments.id = :transactionId LIMIT 1")
    Optional<Payer> findByTransactionId(@Bind("transactionId") Long transactionId);

    @SqlQuery("SELECT * FROM payers p WHERE p.mandate_id = :mandateId")
    Optional<Payer> findByMandateId(@Bind("mandateId") Long mandateId);
    
    @SqlUpdate("INSERT INTO payers(mandate_id, external_id, name, email, bank_account_number_last_two_digits, bank_account_requires_authorisation, bank_account_number, bank_account_sort_code, bank_name, created_date ) VALUES (:mandateId, :externalId, :name, :email, :accountNumberLastTwoDigits, :accountRequiresAuthorisation, :accountNumber, :sortCode, :bankName, :createdDate)")
    @GetGeneratedKeys
    Long insert(@BindBean Payer payer);

    @SqlUpdate("UPDATE payers p SET name = :name,  email = :email, bank_account_number_last_two_digits = :accountNumberLastTwoDigits, bank_account_requires_authorisation = :accountRequiresAuthorisation, bank_account_number = :accountNumber, bank_account_sort_code = :sortCode, bank_name = :bankName WHERE p.id = :id")
    int updatePayerDetails(@Bind("id")Long id, @BindBean Payer payer);
}
