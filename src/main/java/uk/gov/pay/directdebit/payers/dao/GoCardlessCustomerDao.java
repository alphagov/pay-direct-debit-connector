package uk.gov.pay.directdebit.payers.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payers.dao.mapper.GoCardlessCustomerMapper;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;

@RegisterRowMapper(GoCardlessCustomerMapper.class)
public interface GoCardlessCustomerDao {
    @SqlUpdate("INSERT INTO gocardless_customers(payer_id, customer_id, customer_bank_account_id) VALUES (:payerId, :customerId, :customerBankAccountId)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessCustomer goCardlessCustomer);

    @SqlUpdate("UPDATE gocardless_customers g " +
            "SET customer_bank_account_id = :customerBankAccountId, " +
            "customer_id = :customerId " +
            "WHERE g.id = :id")
    int update(@BindBean GoCardlessCustomer goCardlessCustomer);
}
