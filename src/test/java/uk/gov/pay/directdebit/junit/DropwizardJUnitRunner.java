package uk.gov.pay.directdebit.junit;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.testing.ConfigOverride;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.dropwizard.testing.ConfigOverride.config;
import static java.util.Arrays.stream;
import static uk.gov.pay.directdebit.junit.DropwizardTestApplications.createIfNotRunning;
import static uk.gov.pay.directdebit.junit.PostgresTemplate.restoreTemplate;

/**
 * Runs a Dropwizard application with the given {@link DropwizardConfig} before the Test class if there is not an
 * application started yet (from a previous Test class using the same runner) with the same {@link DropwizardConfig}.
 * <p>
 * Example:
 * <p>
 * <pre>
 *  &#64;RunWith(DropwizardJUnitRunner.class)
 *  &#64;DropwizardConfig(app = MyApp.class, config = "config/test.yaml")
 *  public class MyTest {
 *
 *      &#64;Test
 *      public void shouldGetTestResourceFromARunningApplicationInAnotherClass() throws Exception {
 *          given().get("/test-resource")
 *                 .then()
 *                 .statusCode(200);
 *      }
 *  }
 * </pre>
 * <p>
 * After this test and if more tests are being executed, the application will be kept alive so other tests that needs
 * similar configuration will benefit from the existing running application.
 */
public final class DropwizardJUnitRunner extends BlockJUnit4ClassRunner {

    public DropwizardJUnitRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    public static final DataSourceFactory getDbConfig() {
        return DropwizardTestApplications.getFirstPostgresConfig();
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {
        DropwizardConfig dropwizardConfigAnnotation = dropwizardConfigAnnotation();
        List<ConfigOverride> configOverride = newArrayList();
        if (dropwizardConfigAnnotation.withDockerPostgres()) {
            PostgresTestDocker.getOrCreate(dropwizardConfigAnnotation.postgresDockerImage());
            configOverride.add(config("database.url", PostgresTestDocker.getDbRootUri() + PostgresTestDocker.DB_NAME));
            configOverride.add(config("database.user", PostgresTestContainer.DB_USERNAME));
            configOverride.add(config("database.password", PostgresTestContainer.DB_PASSWORD));
        }
        createIfNotRunning(dropwizardConfigAnnotation.app(), dropwizardConfigAnnotation.config(), configOverride.toArray(new ConfigOverride[0]));
        return super.classBlock(notifier);
    }

    @Override
    public Object createTest() throws Exception {
        Object testInstance = super.createTest();
        List<FrameworkField> annotatedFields = getTestClass().getAnnotatedFields();

        annotatedFields.forEach(frameworkField -> stream(frameworkField.getAnnotations())
                .filter(annotation -> annotation.annotationType().equals(DropwizardTestContext.class))
                .findFirst().ifPresent(annotation1 -> {
                    frameworkField.getField().setAccessible(true);
                    try {
                        DropwizardConfig dropwizardConfigAnnotation = dropwizardConfigAnnotation();
                        frameworkField.getField().set(testInstance, new TestContext(DropwizardTestApplications.getPort(dropwizardConfigAnnotation.app(), dropwizardConfigAnnotation.config()),
                                DropwizardTestApplications.getFirstPostgresConfig()));
                    } catch (IllegalAccessException e) {
                        throw new DropwizardJUnitRunnerException(e);
                    }
                }));


        restoreTemplate(getDbConfig());
        return testInstance;
    }

    private DropwizardConfig dropwizardConfigAnnotation() {
        return (DropwizardConfig) stream(getTestClass().getAnnotations())
                .filter(annotation -> annotation.annotationType().equals(DropwizardConfig.class))
                .findFirst()
                .orElseThrow(() -> new DropwizardJUnitRunnerException("DropwizardJUnitRunner requires annotation @DropwizardConfig to be present"));
    }

    //private void restoreDropwizardsLogging() {
    //    appRule.getConfiguration().getLoggingFactory()
    //            .configure(appRule.getEnvironment().metrics(), appRule.getApplication().getName());
    //}
}
