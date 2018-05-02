package uk.gov.pay.directdebit.payments.api;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

public class PaymentViewValidator {

    private static final Long MAX_PAGE_NUMBER = 500l;
    private static final Long DEFAULT_PAGE_NUMBER = 1l;

    public static Pair<Long, Long> validatePagination(Optional<Pair<Long, Long>> paginationParams) {
        Long pageNumber = DEFAULT_PAGE_NUMBER;
        Long displaySize = MAX_PAGE_NUMBER;
        if (paginationParams.isPresent()) {
            displaySize = paginationParams.get().getRight() > MAX_PAGE_NUMBER ? MAX_PAGE_NUMBER : paginationParams.get().getRight();
            pageNumber = (paginationParams.get().getLeft() - 1) * displaySize;
        }
        return Pair.of(pageNumber, displaySize);
    }

    public Optional<List> validateQueryParams(List<Pair<String, Long>> nonNegativePairMap) {
        Map<String, String> invalidQueryParams = new HashMap<>();

        invalidQueryParams.putAll(validateNonNegativeQueryParams(nonNegativePairMap, invalidQueryParams));

        if (!invalidQueryParams.isEmpty()) {
            List<String> invalidResponse = newArrayList();
            invalidResponse.addAll(invalidQueryParams.keySet()
                    .stream()
                    .map(param -> String.format(invalidQueryParams.get(param), param))
                    .collect(Collectors.toList()));
            return Optional.of(invalidResponse);
        }
        return Optional.empty();
    }

    private Map<String, String> validateNonNegativeQueryParams(List<Pair<String, Long>> nonNegativePairMap, Map<String, String> invalidQueryParams) {
        nonNegativePairMap.forEach(param -> {
            if (param.getRight() != null && param.getRight() < 1) {
                invalidQueryParams.put(param.getLeft(), "query param '%s' should be a non zero positive integer");
            }
        });
        return invalidQueryParams;
    }
}
