package uk.gov.pay.directdebit.payments.params;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class CommaDelimitedSetParameter {

    private Set<String> elements = newHashSet();

    public CommaDelimitedSetParameter(String queryString) {
        if (isNotBlank(queryString)) {
            elements.addAll(Arrays.asList(queryString.split(",")));
        }
    }

    public Stream<String> stream() {
        return elements.stream();
    }

    public boolean has(String element) {
        return elements.contains(element);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }
}
