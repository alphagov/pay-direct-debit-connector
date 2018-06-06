package uk.gov.pay.directdebit.payments.dao;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import uk.gov.pay.directdebit.payments.dao.mapper.PaymentViewMapper;
import uk.gov.pay.directdebit.payments.model.PaymentView;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

public class PaymentViewDao {
    private final Jdbi jdbi;
    private final String QUERY_STRING = "SELECT ga.external_id as gateway_external_id, " +
            "t.external_id AS payment_request_id, " +
            "t.amount AS amount, " +
            "t.reference AS reference, " +
            "t.description AS description, " +
            "t.created_date as created_date, " +
            "t.state AS state, " +
            "pa.name AS name, " +
            "pa.email AS email " +
            "FROM transactions t " +
            "INNER JOIN mandates m ON t.mandate_id = m.id " +
            "INNER JOIN gateway_accounts ga ON ga.id = m.gateway_account_id " +
            "LEFT JOIN payers pa ON t.mandate_id = pa.mandate_id " +
            "WHERE ga.external_id = :gatewayAccountExternalId " +
            ":searchExtraFields " +
            "ORDER BY t.id DESC OFFSET :offset LIMIT :limit";

    @Inject
    public PaymentViewDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<PaymentView> searchPaymentView(PaymentViewSearchParams searchParams) {
        String searchExtraFields = searchParams.generateQuery();
        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(QUERY_STRING.replace(":searchExtraFields", searchExtraFields));
            searchParams.getQueryMap().forEach(query::bind);
            return query
                    .map(new PaymentViewMapper())
                    .list();
        });
    }
}
