package uk.gov.pay.directdebit.payers.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.pay.directdebit.payers.dao.mapper.GoCardlessCustomerMapper;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;

import java.util.Optional;

@RegisterMapper(GoCardlessCustomerMapper.class)
public interface GoCardlessCustomerDao {
    @SqlQuery("SELECT * FROM gocardless_customers g WHERE g.id = :id")
    @SingleValueResult(GoCardlessCustomer.class)
    Optional<GoCardlessCustomer> findById(@Bind("id") Long id);

    @SqlUpdate("INSERT INTO gocardless_customers(payer_id, customer_id, customer_bank_account_id) VALUES (:payerId, :customerId, :customerBankAccountId)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessCustomer goCardlessCustomer);

    @SqlUpdate("UPDATE gocardless_customers g SET customer_bank_account_id = :customerBankAccountId WHERE g.id = :id")
    int updateBankAccountId(@Bind("id") Long id, @Bind("customerBankAccountId") String accountId);
}
