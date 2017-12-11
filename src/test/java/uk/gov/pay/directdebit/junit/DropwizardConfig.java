package uk.gov.pay.directdebit.junit;

import io.dropwizard.Application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mandatory annotation when using {@link DropwizardJUnitRunner} to configure the
 * Dropwizard Application to run for testing
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
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DropwizardConfig {

    /**
     * Class of Dropwizard App to run
     * <p>
     * Example: MyDropwizardApplication.class
     *
     * @return java.lang.Class
     */
    Class<? extends Application<?>> app();

    /**
     * Classpath config location of the config test file of the Application to run
     * <p>
     * Example: config/test.yaml
     *
     * @return String
     */
    String config();

    /**
     * Set to true if running Postgres with Dropwizard app, defaults to true
     *
     * @return boolean
     */
    boolean withDockerPostgres() default true;

    /**
     * Docker Postgres image, defaults to govukpay/postgres:9.4.4
     *
     * @return String
     */
    String postgresDockerImage() default "govukpay/postgres:9.4.4";
}
