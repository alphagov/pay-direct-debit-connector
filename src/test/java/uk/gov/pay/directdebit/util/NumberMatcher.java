package uk.gov.pay.directdebit.util;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import static java.lang.String.format;

public class NumberMatcher extends BaseMatcher<Number> {
    private final Number expectation;
    private String actualClass;

    private NumberMatcher(Number expectation) {
        super();
        this.expectation = expectation;
    }

    public static NumberMatcher isNumber(Number expectation) {
        return new NumberMatcher(expectation);
    }

    @Override
    public boolean matches(Object item) {
        if (item instanceof Number) {
            return expectation.longValue() == ((Number) item).longValue();
        }
        actualClass = item.getClass().getSimpleName();
        return false;
    }

    @Override
    public void describeTo(Description description) {
        if (StringUtils.isNotBlank(actualClass)) {
            description.appendText(format("A Number type, received type: [%s].", actualClass));
        } else {
            description.appendValue(expectation);
        }
    }
}
