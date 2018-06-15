package uk.gov.pay.directdebit.payments.dao;

import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.dao.mapper.TransactionMapper;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

@RegisterRowMapper(TransactionMapper.class)
public interface TransactionDao {

    String joinQuery = "SELECT" +
            "  t.id AS transaction_id," +
            "  t.mandate_id AS transaction_mandate_id," +
            "  t.external_id AS transaction_external_id," +
            "  t.amount AS transaction_amount," +
            "  t.state AS transaction_state," +
            "  t.description AS transaction_description," +
            "  t.reference AS transaction_reference," +
            "  t.created_date AS transaction_created_date," +
            "  m.id AS mandate_id," +
            "  m.external_id AS mandate_external_id," +
            "  m.reference AS mandate_reference," +
            "  m.gateway_account_id AS mandate_gateway_account_id," +
            "  m.return_url AS mandate_return_url," +
            "  m.type AS mandate_type," +
            "  m.state AS mandate_state," +
            "  m.created_date AS mandate_created_date," +
            "  g.id AS gateway_account_id," +
            "  g.external_id AS gateway_account_external_id," +
            "  g.payment_provider AS gateway_account_payment_provider," +
            "  g.service_name AS gateway_account_service_name," +
            "  g.type AS gateway_account_type," +
            "  g.description AS gateway_account_description," +
            "  g.analytics_id AS gateway_account_analytics_id," +
            "  y.id AS payer_id," +
            "  y.mandate_id AS payer_mandate_id," +
            "  y.external_id AS payer_external_id," +
            "  y.name AS payer_name," +
            "  y.email AS payer_email," +
            "  y.bank_account_number_last_two_digits AS payer_bank_account_number_last_two_digits," +
            "  y.bank_account_requires_authorisation AS payer_bank_account_requires_authorisation," +
            "  y.bank_account_number AS payer_bank_account_number," +
            "  y.bank_account_sort_code AS payer_bank_account_sort_code," +
            "  y.bank_name AS payer_bank_name," +
            "  y.created_date AS payer_created_date" +
            " FROM transactions t " + 
            "  JOIN mandates m ON t.mandate_id = m.id" +
            "  JOIN gateway_accounts g ON m.gateway_account_id = g.id" +
            "  LEFT JOIN payers y ON y.mandate_id = t.mandate_id";
    
    @SqlQuery(joinQuery + " WHERE t.id = :id")
    Optional<Transaction> findById(@Bind("id") Long id);

    @SqlQuery(joinQuery + " WHERE t.external_id = :externalId")
    Optional<Transaction> findByExternalId(@Bind("externalId") String externalId);

    @SqlQuery(joinQuery + " JOIN tokens k ON k.mandate_id = m.id WHERE k.secure_redirect_token = :tokenId")
    Optional<Transaction> findByTokenId(@Bind("tokenId") String tokenId);

    @SqlQuery(joinQuery + " WHERE t.state = :state AND g.payment_provider = :paymentProvider")
    List<Transaction> findAllByPaymentStateAndProvider(@Bind("state") PaymentState paymentState, @Bind("paymentProvider") PaymentProvider paymentProvider);

    @SqlQuery(joinQuery + " WHERE m.external_id = :mandateExternalId")
    List<Transaction> findAllByMandateExternalId(@Bind("mandateExternalId") String mandateExternalId);

    @SqlUpdate("UPDATE transactions t SET state = :state WHERE t.id = :id")
    int updateState(@Bind("id") Long id, @Bind("state") PaymentState paymentState);

    @SqlUpdate("INSERT INTO transactions(mandate_id, external_id, amount, state, type, description, reference, created_date) VALUES (:mandate.id, :externalId, :amount, :state, 'charge',:description, :reference, :createdDate)")
    @GetGeneratedKeys
    Long insert(@BindBean Transaction transaction);

}
