package uk.gov.pay.directdebit.payments.dao;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import uk.gov.pay.directdebit.payments.dao.mapper.PaymentViewMapper;
import uk.gov.pay.directdebit.payments.model.PaymentView;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class PaymentViewDao {

    private final Jdbi jdbi;
    private final String QUERY_STRING = "SELECT ga.external_id as gateway_external_id, " +
            "pr.external_id AS payment_request_id, " +
            "pr.amount AS amount, " +
            "pr.reference AS reference, " +
            "pr.description AS description, " +
            "pr.return_url as return_url, " +
            "pr.created_date as created_date, " +
            "pa.name AS name, " +
            "pa.email AS email, " +
            "tr.state AS state " +
            "FROM payment_requests pr " +
            "JOIN payers pa ON pr.id = pa.payment_request_id " +
            "JOIN transactions tr ON pr.id = tr.payment_request_id " +
            "JOIN gateway_accounts ga ON ga.id = pr.gateway_account_id " +
            "WHERE ga.external_id = :gatewayAccountExternalId " +
            ":searchExtraFields " +
            "ORDER BY pr.id DESC OFFSET :offset LIMIT :limit";

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
