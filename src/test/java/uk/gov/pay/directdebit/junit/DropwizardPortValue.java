package uk.gov.pay.directdebit.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional field level annotation that injects an int value of the port of the given Dropwizard application.
 * Useful if unknown, such as a configuration that assigns an available port value like `server.applicationConnectors[0].port=0`
 *
 * Example:
 *
 * <pre>
 *  &#64;RunWith(DropwizardJUnitRunner.class)
 *  &#64;DropwizardConfig(app = MyApp.class, config = "config/test.yaml")
 *  public class MyTest {
 *
 *      &#64;DropwizardPortValue
 *      private int port;
 *
 *      &#64;Test
 *      public void shouldGetTestResourceFromARunningApplicationInAnotherClass() throws Exception {
 *          given().get("http://localhost:" + port + "/test-resource")
 *                 .then()
 *                 .statusCode(200);
 *      }
 *  }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DropwizardPortValue {
}
