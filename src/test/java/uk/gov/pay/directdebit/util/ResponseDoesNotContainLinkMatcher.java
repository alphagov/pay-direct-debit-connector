package uk.gov.pay.directdebit.util;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ResponseDoesNotContainLinkMatcher extends TypeSafeMatcher<List<Map<String, Object>>> {

    private final String rel;

    private ResponseDoesNotContainLinkMatcher(String rel) {
        checkNotNull(rel);
        this.rel = rel;
    }

    public static ResponseDoesNotContainLinkMatcher doesNotContainLink(String rel) {
        return new ResponseDoesNotContainLinkMatcher(rel);
    }

    @Override
    protected boolean matchesSafely(List<Map<String, Object>> links) {
        return links.stream().noneMatch(link -> this.rel.equals(link.get("rel")));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Link with rel does not exist: {rel=").appendValue(rel).appendText("}");
    }
}
