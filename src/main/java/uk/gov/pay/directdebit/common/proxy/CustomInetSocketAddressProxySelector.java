package uk.gov.pay.directdebit.common.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Custom {@link java.net.ProxySelector ProxySelector} which will use only one proxy and will return new instance each time.
 *
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html">Java Networking and Proxies</a>
 */
public class CustomInetSocketAddressProxySelector extends ProxySelector {

    /**
     * Keep a reference on the previous default
     */
    private ProxySelector defaultProxySelector;

    /**
     * Proxy hostname to be used
     */
    private String proxyHostname;

    /**
     * Proxy port to be used
     */
    private int proxyPort;

    /**
     * CustomInetSocketAddressProxySelector constructor
     *
     * @param defaultProxySelector default proxy selector
     * @param proxyHostname        proxy's hostname
     * @param proxyPort            proxy's port
     */
    public CustomInetSocketAddressProxySelector(ProxySelector defaultProxySelector,
                                                String proxyHostname,
                                                int proxyPort) {
        this.defaultProxySelector = defaultProxySelector;
    }

    @Override
    public List<Proxy> select(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI can't be null.");
        }

        /*
         * If it's a http (or https) URL, then we use our own list.
         */
        String protocol = uri.getScheme();
        if ((this.proxyHostname != null) &&
                ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol))) {
            return Collections.singletonList(
                    new Proxy(
                            Proxy.Type.HTTP,
                            new InetSocketAddress(this.proxyHostname, this.proxyPort)
                    )
            );
        }

        /*
         * Not HTTP or HTTPS (could be SOCKS or FTP) defer to the default selector.
         */
        if (defaultProxySelector != null) {
            return defaultProxySelector.select(uri);
        } else {
            return Collections.singletonList(Proxy.NO_PROXY);
        }
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        if (uri == null || sa == null || ioe == null) {
            throw new IllegalArgumentException("Arguments can't be null.");
        }

        if (defaultProxySelector != null) {
            defaultProxySelector.connectFailed(uri, sa, ioe);
        }
    }

}
