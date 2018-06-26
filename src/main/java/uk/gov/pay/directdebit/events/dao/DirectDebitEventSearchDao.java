package uk.gov.pay.directdebit.events.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.params.DirectDebitEventSearchParams;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class DirectDebitEventSearchDao {

    private static final String QUERY = "SELECT * from EVENTS e WHERE :searchFields";
    
    private final Jdbi jdbi;

    @Inject
    public DirectDebitEventSearchDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }
    
    public List<DirectDebitEvent> findEvents(DirectDebitEventSearchParams searchParams) {
        QueryStringAndQueryMap queryStringAndQueryMap = generateQuery(searchParams);
        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(QUERY.replace(":searchFields", queryStringAndQueryMap.queryString));
            queryStringAndQueryMap.queryMap.forEach(query::bind);
            return query.map(new DirectDebitEventsMapper()).list();
        });
    }

    private QueryStringAndQueryMap generateQuery(DirectDebitEventSearchParams searchParams) {
        List<String> searchStrings = Lists.newArrayList();
        Map<String, Object> queryMap = Maps.newHashMap();
        if (nonNull(searchParams.getMandateId())) {
            searchStrings.add("e.mandate_id = :mandate_id");
            queryMap.put("mandate_id", searchParams.getMandateId());
        }
        if (nonNull(searchParams.getTransactionId())) {
            searchStrings.add("e.transaction_id = :transaction_id");
            queryMap.put("transaction_id", searchParams.getTransactionId());
        }
        if (nonNull(searchParams.getBeforeDate())) {
            searchStrings.add("e.event_date < :before_date");
            queryMap.put("before_date", searchParams.getBeforeDate());
        }
        if (nonNull(searchParams.getAfterDate())) {
            searchStrings.add("e.event_date > :after_date");
            queryMap.put("after_date", searchParams.getAfterDate());
        }
        return new QueryStringAndQueryMap(searchStrings.stream().collect(Collectors.joining(" AND ")), queryMap);
    }

    private class DirectDebitEventsMapper implements RowMapper<DirectDebitEvent> {
        @Override
        public DirectDebitEvent map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new DirectDebitEvent(
                    rs.getLong("id"),
                    rs.getLong("mandate_id"),
                    rs.getLong("transaction_id"),
                    DirectDebitEvent.Type.valueOf(rs.getString("event_type")),
                    DirectDebitEvent.SupportedEvent.valueOf(rs.getString("event")),
                    ZonedDateTime.ofInstant(rs.getTimestamp("event_date").toInstant(), ZoneOffset.UTC));
        }
    }

    @AllArgsConstructor
    private class QueryStringAndQueryMap {
        public final String queryString; 
        public final Map<String, Object> queryMap;
    }
}
