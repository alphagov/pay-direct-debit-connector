package uk.gov.pay.directdebit.mandate.dao;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import uk.gov.pay.directdebit.mandate.dao.mapper.MandateMapper;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.params.MandateSearchParams;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MandateSearchDao {
    
    private final Jdbi jdbi;

    @Inject
    public MandateSearchDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<Mandate> search(MandateSearchParams mandateSearchParams, String gatewayAccountExternalId) {
        return jdbi.withHandle(handle -> {
            var sqlQueryAndParameters = createSqlQuery(mandateSearchParams, gatewayAccountExternalId, SearchMode.SELECT);

            Query query = handle.createQuery(sqlQueryAndParameters.query);
            return bindQuery(query, sqlQueryAndParameters).map(new MandateMapper()).list();
        });
    }

    public int countTotalMatchingMandates(MandateSearchParams mandateSearchParams, String gatewayAccountExternalId) {
        return jdbi.withHandle(handle -> {
            var sqlQueryAndParameters = createSqlQuery(mandateSearchParams, gatewayAccountExternalId, SearchMode.COUNT);

            Query query = handle.createQuery(sqlQueryAndParameters.query);
            return bindQuery(query, sqlQueryAndParameters).mapTo(Integer.class).findOnly();
        });
    }

    private Query bindQuery(Query query, SqlStatementAndParameters sqlStatementAndParameters) {
        if (sqlStatementAndParameters.parameters.containsKey("states")) {
            @SuppressWarnings("unchecked")
            List<String> states = (List<String>) sqlStatementAndParameters.parameters.get("states");

            query.bindList("states", states);
            sqlStatementAndParameters.parameters.remove("states");
        }
        sqlStatementAndParameters.parameters.forEach(query::bind);
        return query;
    }

    private SqlStatementAndParameters createSqlQuery(MandateSearchParams params, String gatewayAccountExternalId, SearchMode searchMode) {
        var sql = new StringBuilder(2048);
        if (searchMode.equals(SearchMode.COUNT)) {
            sql.append("SELECT COUNT(*) ");
        } else {
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
                    "  p.created_date AS payer_created_date");
        }

        sql.append(" FROM mandates m JOIN gateway_accounts g ON g.id = m.gateway_account_id " +
                "  LEFT JOIN payers p ON p.mandate_id = m.id ");

        Map<String, Object> sqlParams = new HashMap<>();
        
        sql.append(" WHERE g.external_id = :gatewayAccountExternalId");
        sqlParams.put("gatewayAccountExternalId", gatewayAccountExternalId);
        
        params.getServiceReference().filter(s -> !s.isBlank()).ifPresent(serviceReference -> {
            sql.append(" AND m.service_reference ILIKE :serviceReference");
            sqlParams.put("serviceReference", "%" + serviceReference + "%");
        });

        params.getMandateBankStatementReference().filter(s -> !s.toString().isBlank()).ifPresent(bankStatementRef -> {
            sql.append(" AND m.mandate_reference ILIKE :mandateRef");
            sqlParams.put("mandateRef", "%" + bankStatementRef.toString() + "%");
        });

        params.getName().filter(s -> !s.isBlank()).ifPresent(name -> {
            sql.append(" AND p.name ILIKE :name");
            sqlParams.put("name", "%" + name + "%");
        });

        params.getEmail().filter(s -> !s.isBlank()).ifPresent(email -> {
            sql.append(" AND p.email ILIKE :email");
            sqlParams.put("email", "%" + email + "%");
        });

        params.getExternalMandateState().ifPresent(mandateState -> {
            sql.append(" AND m.state IN (<states>)");
            sqlParams.put("states", params.getInternalStates());
        });

        params.getFromDate().ifPresent(fromDate -> {
            sql.append(" AND m.created_date > :createdDateFrom");
            sqlParams.put("createdDateFrom", fromDate);
        });

        params.getToDate().ifPresent(toDate -> {
            sql.append(" AND m.created_date < :createdDateTo");
            sqlParams.put("createdDateTo", toDate);
        });

        if (searchMode.equals(SearchMode.SELECT)) {
            sql.append(" ORDER BY m.id DESC OFFSET :offset LIMIT :limit");
            sqlParams.put("limit", params.getDisplaySize());
            sqlParams.put("offset", calculateOffset(params));
        }

        return new SqlStatementAndParameters(sql.toString(), sqlParams);
    }

    private int calculateOffset(MandateSearchParams params) {
        return params.getPage() == 1 ? 0 : (params.getPage() - 1) * params.getDisplaySize();
    }
    
    private static class SqlStatementAndParameters {
        private final String query;
        private final Map<String, Object> parameters;

        SqlStatementAndParameters(String query, Map<String, Object> parameters) {
            this.query = query;
            this.parameters = parameters;
        }
    }
    
    private enum SearchMode {COUNT, SELECT};
}
