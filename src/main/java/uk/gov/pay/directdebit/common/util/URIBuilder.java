package uk.gov.pay.directdebit.common.util;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;

public class URIBuilder {


    public static URI selfUriFor(UriInfo uriInfo, String path, String... ids) {
        return uriInfo.getBaseUriBuilder()
                .path(path)
                .build((Object[]) ids);
    }

    public static URI nextUrl(String baseUrl, String... paths) {
        UriBuilder uriBuilder = UriBuilder.fromUri(baseUrl);
        for (String path: paths) {
            uriBuilder = uriBuilder.path(path);
        }
        return uriBuilder.build();
    }

    public static Map<String, Object> createLink(String rel, String method, URI href) {
        return ImmutableMap.of(
                "rel", rel,
                "method", method,
                "href", href
        );
    }

    public static Map<String, Object> createLink(String rel, String method, URI href, String type, Map<String, Object> params) {
        return ImmutableMap.of(
                "rel", rel,
                "method", method,
                "href", href,
                "type", type,
                "params", params
        );
    }
}
