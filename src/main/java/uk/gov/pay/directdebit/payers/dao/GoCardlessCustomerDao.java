package uk.gov.pay.directdebit.payers.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payers.dao.mapper.GoCardlessCustomerMapper;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;

import java.util.Optional;

@RegisterRowMapper(GoCardlessCustomerMapper.class)
public interface GoCardlessCustomerDao {
    @SqlQuery("SELECT * FROM gocardless_customers g WHERE g.payer_id = :id")
    Optional<GoCardlessCustomer> findByPayerId(@Bind("id") Long id);

    @SqlUpdate("INSERT INTO gocardless_customers(payer_id, customer_id, customer_bank_account_id) VALUES (:payerId, :customerId, :customerBankAccountId)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessCustomer goCardlessCustomer);

    @SqlUpdate("UPDATE gocardless_customers g SET customer_bank_account_id = :customerBankAccountId WHERE g.id = :id")
    int updateBankAccountId(@Bind("id") Long id, @Bind("customerBankAccountId") String accountId);
}
