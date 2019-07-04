package uk.gov.pay.directdebit.mandate.dao;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import uk.gov.pay.directdebit.mandate.dao.mapper.MandateMapper;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.params.MandateSearchParams;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MandateSearchDao {
    
    private final Jdbi jdbi;

    @Inject
    public MandateSearchDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<Mandate> search(MandateSearchParams mandateSearchParams) {
        var sql = new StringBuilder(2048);
        sql.append("SELECT DISTINCT" +
                "  m.id AS mandate_id," +
                "  m.external_id AS mandate_external_id," +
                "  m.mandate_reference AS mandate_mandate_reference," +
                "  m.service_reference AS mandate_service_reference," +
                "  m.gateway_account_id AS mandate_gateway_account_id," +
                "  m.return_url AS mandate_return_url," +
                "  m.state AS mandate_state," +
                "  m.created_date AS mandate_created_date," +
                "  m.payment_provider_id AS mandate_payment_provider_id," +
                "  g.id AS gateway_account_id," +
                "  g.external_id AS gateway_account_external_id," +
                "  g.payment_provider AS gateway_account_payment_provider," +
                "  g.type AS gateway_account_type," +
                "  g.description AS gateway_account_description," +
                "  g.analytics_id AS gateway_account_analytics_id," +
                "  g.access_token AS gateway_account_access_token," +
                "  g.organisation AS gateway_account_organisation," +
                "  p.id AS payer_id," +
                "  p.mandate_id AS payer_mandate_id," +
                "  p.external_id AS payer_external_id," +
                "  p.name AS payer_name," +
                "  p.email AS payer_email," +
                "  p.bank_account_number_last_two_digits AS payer_bank_account_number_last_two_digits," +
                "  p.bank_account_requires_authorisation AS payer_bank_account_requires_authorisation," +
                "  p.bank_account_number AS payer_bank_account_number," +
                "  p.bank_account_sort_code AS payer_bank_account_sort_code," +
                "  p.bank_name AS payer_bank_name," +
                "  p.created_date AS payer_created_date" +
                " FROM mandates m" +
                "  JOIN gateway_accounts g ON g.id = m.gateway_account_id " +
                "  LEFT JOIN payers p ON p.mandate_id = m.id " +
                " WHERE g.external_id = :gatewayAccountExternalId");
        //            "  ORDER BY p.id DESC OFFSET :offset LIMIT :limit";

        var sqlParams = new HashMap<String, Object>();
        sqlParams.put("gatewayAccountExternalId", mandateSearchParams.getGatewayAccountExternalId());

        mandateSearchParams.getReference().filter(s -> !s.isBlank()).ifPresent(serviceReference -> {
            sql.append(" AND m.service_reference ILIKE :serviceReference");
            sqlParams.put("serviceReference", "%" + serviceReference + "%");
        });
        
        mandateSearchParams.getMandateBankStatementReference().ifPresent(bankStatementRef -> {
            sql.append(" AND m.mandate_reference ILIKE :mandateRef");
            sqlParams.put("mandateRef", "%" + bankStatementRef.toString() + "%");
        });
        
        mandateSearchParams.getName().filter(s -> !s.isBlank()).ifPresent(name -> {
            sql.append(" AND p.name ILIKE :name");
            sqlParams.put("name", "%" + name + "%");
        });
        
        mandateSearchParams.getEmail().filter(s -> !s.isBlank()).ifPresent(email -> {
            sql.append(" AND p.email ILIKE :email");
            sqlParams.put("email", "%" + email + "%");
        });
        
        mandateSearchParams.getMandateState().ifPresent(mandateState -> {
            sql.append(" AND m.state = :state");
            sqlParams.put("state", mandateState);
        });
        
        mandateSearchParams.getFromDate().ifPresent(fromDate -> {
            sql.append(" AND m.created_date > :createdDateFrom");
            sqlParams.put("createdDateFrom", fromDate);
        });
        
        mandateSearchParams.getToDate().ifPresent(toDate -> {
            sql.append(" AND m.created_date < :createdDateTo");
            sqlParams.put("createdDateTo", toDate);
        });

        sql.append(" ORDER BY m.id DESC LIMIT :limit");
        sqlParams.put("limit", mandateSearchParams.getDisplaySize());
        
        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(sql.toString());
            sqlParams.forEach(query::bind);
            return query.map(new MandateMapper()).list();
        });
    }
}
