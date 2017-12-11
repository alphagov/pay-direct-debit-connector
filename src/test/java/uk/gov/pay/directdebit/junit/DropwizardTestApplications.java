package uk.gov.pay.directdebit.junit;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Runtime.getRuntime;
import static uk.gov.pay.directdebit.infra.PostgresTemplate.createTemplate;

/**
 * Runs and hold current Dropwizard Applications running
 *
 * @see <a href="http://www.dropwizard.io/1.2.0/docs/manual/testing.html">http://www.dropwizard.io/1.2.0/docs/manual/testing.html<a/>
 * <p>
 * - Keeps a single instance of a Dropwizard application per configuration values that differs from existing ones,
 * otherwise will create a new instance with the new configuration. So those test classes using {@link DropwizardJUnitRunner}
 * and annotating the configuration similar to other test classes won't create new applications but reuse the first that
 * started up.
 */
final class DropwizardTestApplications {

    private static final Map<Pair<Class<? extends Application>, String>, DropwizardTestSupport> apps = new ConcurrentHashMap<>();

    static {
        getRuntime().addShutdownHook(new Thread(() -> {
            for (DropwizardTestSupport applicationsRunning : apps.values()) {
                applicationsRunning.after();
            }
        }));
    }

    static void createIfNotRunning(Class<? extends Application> appClass, String configClasspathLocation, ConfigOverride... configOverrides) {
        Pair<Class<? extends Application>, String> key = Pair.of(appClass, configClasspathLocation);
        if (!apps.containsKey(key)) {
            String resourceConfigFilePath = ResourceHelpers.resourceFilePath(configClasspathLocation);
            DropwizardTestSupport newApp = new DropwizardTestSupport(appClass,
                    resourceConfigFilePath,
                    configOverrides);
            apps.put(key, newApp);
            newApp.before();
            try {
                newApp.getApplication().run("db", "migrate", resourceConfigFilePath);
                createTemplate(((DirectDebitConfig)newApp.getConfiguration()).getDataSourceFactory());
            } catch (Exception e) {
                throw new DropwizardJUnitRunnerException(e);
            }
        }
    }

    static int getPort(Class<? extends Application<?>> appClass, String configClasspathLocation) {
        Pair<Class<? extends Application>, String> key = Pair.of(appClass, configClasspathLocation);
        return apps.get(key).getLocalPort();
    }

    static DataSourceFactory getFirstPostgresConfig() {
        if(!apps.isEmpty()) {
            return ((DirectDebitConfig) apps.values().iterator().next().getConfiguration()).getDataSourceFactory();
        }
        return null;
    }

}
