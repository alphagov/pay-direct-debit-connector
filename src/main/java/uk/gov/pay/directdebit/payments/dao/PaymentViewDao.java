package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.dao.mapper.PaymentResponseMapper;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentViewDao {
    private final Jdbi jdbi;

    @Inject
    public PaymentViewDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<PaymentResponse> searchPaymentView(PaymentViewSearchParams searchParams, String gatewayAccountExternalId) {
        return jdbi.withHandle(handle -> {
            var sqlStatementAndParameters = createSqlQuery(searchParams, gatewayAccountExternalId, SearchMode.SELECT);

            Query query = handle.createQuery(sqlStatementAndParameters.query);
            return bindQuery(query, sqlStatementAndParameters).map(new PaymentResponseMapper()).list();
        });
    }

    public Integer getPaymentViewCount(PaymentViewSearchParams searchParams, String gatewayAccountExternalId) {
        return jdbi.withHandle(handle -> {
            var sqlStatementAndParameters = createSqlQuery(searchParams, gatewayAccountExternalId, SearchMode.COUNT);

            Query query = handle.createQuery(sqlStatementAndParameters.query);
            return bindQuery(query, sqlStatementAndParameters).mapTo(Integer.class).findOnly();
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

    private SqlStatementAndParameters createSqlQuery(PaymentViewSearchParams params, String gatewayAccountExternalId, SearchMode searchMode) {
        var sql = new StringBuilder();
        if (searchMode.equals(SearchMode.COUNT)) {
            sql.append("SELECT COUNT(*) ");
        } else {
            sql.append("SELECT DISTINCT " +
                    " p.id AS payment_id, " +
                    " p.external_id AS payment_external_id, " +
                    " p.amount AS amount, " +
                    " p.reference AS reference, " +
                    " p.description AS description, " +
                    " p.created_date as created_date, " +
                    " p.state AS state, " +
                    " p.state_details AS state_details, " +
                    " p.state_details_description AS state_details_description, " +
                    " p.payment_provider_id as provider_id, " +
                    " ga.external_id as gateway_external_id, " +
                    " ga.payment_provider as payment_provider, " +
                    " m.external_id as mandate_external_id ");
        }
        
        sql.append(" FROM payments p " +
                " INNER JOIN mandates m ON p.mandate_id = m.id " +
                " INNER JOIN gateway_accounts ga ON ga.id = m.gateway_account_id ");
        
        Map<String, Object> sqlParams = new HashMap<>();
        
        sql.append(" WHERE ga.external_id = :gatewayAccountExternalId");
        sqlParams.put("gatewayAccountExternalId", gatewayAccountExternalId);
        
        params.getMandateId().filter(s -> !s.isBlank()).ifPresent(mandateId -> {
            sql.append(" AND m.external_id = :mandateExternalId");
            sqlParams.put("mandateExternalId", mandateId);
        });
        
        params.getReference().filter(s -> !s.isBlank()).ifPresent(reference -> {
            sql.append(" AND p.reference ILIKE :reference");
            sqlParams.put("reference", "%" + reference + "%");
        });
        
        params.getAmount().ifPresent(amount -> {
            sql.append(" AND p.amount = :amount");
            sqlParams.put("amount", amount);
        });
        
        if (!params.getInternalStates().isEmpty()) {
            sql.append(" AND p.state IN (<states>)");
            sqlParams.put("states", params.getInternalStates());
        }

        params.getFromDate().ifPresent(fromDate -> {
            sql.append(" AND p.created_date >= :createdDateFrom");
            sqlParams.put("createdDateFrom", fromDate);
        });

        params.getToDate().ifPresent(toDate -> {
            sql.append(" AND p.created_date < :createdDateTo");
            sqlParams.put("createdDateTo", toDate);
        });
        
        if (searchMode.equals(SearchMode.SELECT)) {
            sql.append(" ORDER BY p.id DESC OFFSET :offset LIMIT :limit");
            sqlParams.put("limit", params.getDisplaySize());
            sqlParams.put("offset", params.getOffset());
        }
        
        return new SqlStatementAndParameters(sql.toString(), sqlParams);
    }
    
    private static class SqlStatementAndParameters {
        private final String query;
        private final Map<String, Object> parameters;

        SqlStatementAndParameters(String query, Map<String, Object> parameters) {
            this.query = query;
            this.parameters = parameters;
        }
    }

    private enum SearchMode {COUNT, SELECT}
}
