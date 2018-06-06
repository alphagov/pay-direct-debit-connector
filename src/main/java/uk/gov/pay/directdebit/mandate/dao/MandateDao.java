package uk.gov.pay.directdebit.mandate.dao;

import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.mandate.dao.mapper.MandateMapper;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;

@RegisterRowMapper(MandateMapper.class)
public interface MandateDao {

    @SqlUpdate("INSERT INTO mandates(external_id, gateway_account_id, reference, state, type, return_url, created_date) VALUES (:externalId, :gatewayAccount.id, :reference, :state, :type, :returnUrl, :createdDate)")
    @GetGeneratedKeys
    Long insert(@BindBean Mandate mandate);

    String query = "SELECT DISTINCT" +
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
            " FROM mandates m" +
            "  JOIN gateway_accounts g ON g.id = m.gateway_account_id " +
            "  LEFT JOIN transactions t ON t.mandate_id = m.id " +
            "  LEFT JOIN payers y ON y.mandate_id = m.id ";
    
    
    @SqlQuery(query + "JOIN tokens k ON k.mandate_id = m.id WHERE k.secure_redirect_token = :tokenId")
    Optional<Mandate> findByTokenId(@Bind("tokenId") String tokenId);
    
    @SqlQuery(query + "WHERE m.id = :mandateId")
    Optional<Mandate> findById(@Bind("mandateId") Long mandateId);

    @SqlQuery(query + "WHERE m.external_id = :externalId")
    Optional<Mandate> findByExternalId(@Bind("externalId") String externalId);
    
    @SqlUpdate("UPDATE mandates m SET state = :state WHERE m.id = :id")
    int updateState(@Bind("id") Long id, @Bind("state") MandateState mandateState);

    @SqlUpdate("UPDATE mandates m SET reference = :reference WHERE m.id = :id")
    int updateReference(@Bind("id") Long id, @Bind("reference") String reference);
}
