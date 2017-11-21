package uk.gov.pay.directdebit.app.ssl;

import org.postgresql.ssl.WrappedFactory;

public class TrustingSSLSocketFactory extends WrappedFactory {
    public TrustingSSLSocketFactory() {
        this._factory = TrustStoreLoader.getSSLContext().getSocketFactory();
    }
}
