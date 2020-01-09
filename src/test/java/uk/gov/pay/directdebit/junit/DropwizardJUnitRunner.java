package uk.gov.pay.directdebit.junit;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import junitparams.JUnitParamsRunner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.util.Arrays.stream;
import static uk.gov.pay.directdebit.junit.DropwizardTestApplications.createIfNotRunning;
import static uk.gov.pay.directdebit.junit.PostgresTemplate.createTemplate;
import static uk.gov.pay.directdebit.junit.PostgresTemplate.restorePostgres;
import static uk.gov.pay.directdebit.junit.PostgresTestDocker.getDbPassword;
import static uk.gov.pay.directdebit.junit.PostgresTestDocker.getDbUri;
import static uk.gov.pay.directdebit.junit.PostgresTestDocker.getDbUsername;
import static uk.gov.pay.directdebit.junit.PostgresTestDocker.getOrCreate;

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
public final class DropwizardJUnitRunner extends JUnitParamsRunner {

    public DropwizardJUnitRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {
        DropwizardConfig dropwizardConfigAnnotation = dropwizardConfigAnnotation();
        List<ConfigOverride> configOverride = newArrayList();
        if (dropwizardConfigAnnotation.withDockerPostgres()) {
            getOrCreate();
            configOverride.add(config("database.url", getDbUri()));
            configOverride.add(config("database.user", getDbUsername()));
            configOverride.add(config("database.password", getDbPassword()));
        }
        try {
            Optional<DropwizardTestSupport> createdApp = createIfNotRunning(dropwizardConfigAnnotation.app(), dropwizardConfigAnnotation.config(), configOverride.toArray(new ConfigOverride[0]));
            if (dropwizardConfigAnnotation.withDockerPostgres() && createdApp.isPresent()) {
                createdApp.get().getApplication().run("db", "migrate", resourceFilePath(dropwizardConfigAnnotation.config()));
                createTemplate(getDbUri(), getDbUsername(), getDbPassword());
            }
        } catch (Exception e) {
            throw new DropwizardJUnitRunnerException(e);
        }

        return super.classBlock(notifier);
    }

    @Override
    public Object createTest() throws Exception {
        Object testInstance = super.createTest();
        DropwizardConfig declaredConfiguration = dropwizardConfigAnnotation();
        TestContext testContext = DropwizardTestApplications.getTestContextOf(declaredConfiguration.app(), declaredConfiguration.config());
        setTestContextToDeclaredAnnotations(testInstance, testContext);
        if (declaredConfiguration.withDockerPostgres()) {
            restorePostgres(testContext.getDatabaseUrl(), testContext.getDatabaseUser(), testContext.getDatabasePassword());
        }
        return testInstance;
    }

    private void setTestContextToDeclaredAnnotations(Object testInstance, TestContext testContext) {
        List<FrameworkField> annotatedFields = getTestClass().getAnnotatedFields();
        annotatedFields.forEach(frameworkField -> stream(frameworkField.getAnnotations())
                .filter(annotation -> annotation.annotationType().equals(DropwizardTestContext.class))
                .findFirst()
                .ifPresent(testContextAnnotation -> {
                    frameworkField.getField().setAccessible(true);
                    try {
                        frameworkField.getField().set(testInstance, testContext);
                    } catch (IllegalAccessException e) {
                        throw new DropwizardJUnitRunnerException(e);
                    }
                }));
    }

    private DropwizardConfig dropwizardConfigAnnotation() {
        return (DropwizardConfig) stream(getTestClass().getAnnotations())
                .filter(annotation -> annotation.annotationType().equals(DropwizardConfig.class))
                .findFirst()
                .orElseThrow(() -> new DropwizardJUnitRunnerException("DropwizardJUnitRunner requires annotation @DropwizardConfig to be present"));
    }
}
