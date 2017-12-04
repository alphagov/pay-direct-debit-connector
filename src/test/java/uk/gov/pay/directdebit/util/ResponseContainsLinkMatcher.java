package uk.gov.pay.directdebit.util;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class ResponseContainsLinkMatcher extends TypeSafeMatcher<List<Map<String, Object>>> {

    private String rel;
    private String method;
    private String href;
    private String type;
    private Map<String, Object> params;

    private ResponseContainsLinkMatcher(String rel, String method, String href) {
        checkNotNull(rel);
        checkNotNull(method);
        checkNotNull(href);
        this.rel = rel;
        this.href = href;
        this.method = method;
    }

    private ResponseContainsLinkMatcher(String rel, String method, String href, String type, Map<String, Object> params) {
        this(rel, method, href);
        checkNotNull(type);
        checkNotNull(params);
        this.type = type;
        this.params = params;
    }

    public static ResponseContainsLinkMatcher containsLink(String rel, String method, String href) {
        return new ResponseContainsLinkMatcher(rel, method, href);
    }

    public static ResponseContainsLinkMatcher containsLink(String rel, String method, String href, String type, Map<String, Object> params) {
        return new ResponseContainsLinkMatcher(rel, method, href, type, params);
    }

    @Override
    protected boolean matchesSafely(List<Map<String, Object>> links) {

        List<Map<String, Object>> filteredLinks = links.stream()
                .filter(link -> this.rel.equals(link.get("rel"))).collect(Collectors.toList());

        boolean result = filteredLinks.size() == 1 &&
                method.equals(filteredLinks.get(0).get("method")) &&
                href.equals(filteredLinks.get(0).get("href"));

        if (type == null) {
            // only rel, method and href and no other fields
            result = result && filteredLinks.get(0).size() == 3;
        } else {
            result = result &&
                    type.equals(filteredLinks.get(0).get("type")) &&
                    parametersMatches(params, (Map<String, Object>) filteredLinks.get(0).get("params"));
        }

        return result;
    }

    private boolean parametersMatches(Map<String, Object> expected, Map<String, Object> actual) {
        return expected.size() == actual.size() &&
                actual.entrySet().containsAll(expected.entrySet());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("One link with exactly these fields: {method=").appendValue(method)
                .appendText(", rel=").appendValue(rel)
                .appendText(", href=").appendValue(href);
        if (type != null) {
            description.appendText(", type=").appendValue(type)
                    .appendText(", params=").appendValue(params);
        }
        description.appendText("}");
    }
}
