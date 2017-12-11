package uk.gov.pay.directdebit.infra;

import io.dropwizard.jdbi.OptionalContainerFactory;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Rule;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.IntegrationTestsSuite;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static uk.gov.pay.directdebit.IntegrationTestsSuite.env;

public abstract class IntegrationTest {

    private final String databaseUrl = env().getConfiguration().getDataSourceFactory().getUrl();
    private final String username = env().getConfiguration().getDataSourceFactory().getUser();
    private final String password = env().getConfiguration().getDataSourceFactory().getPassword();

  //  @Rule
  //  public PostgresResetRule postgresReset = new PostgresResetRule(env());

    protected DBI jdbi;
    protected DatabaseTestHelper databaseTestHelper;

    @Before
    public void setupJDBI() throws Exception {
        jdbi = new DBI(databaseUrl, username, password);
        jdbi.registerContainerFactory(new OptionalContainerFactory());
        databaseTestHelper = new DatabaseTestHelper(jdbi);
    }

    protected RequestSpecification givenSetup() {
        return given().port(IntegrationTestsSuite.env().getPort())
                .contentType(JSON);
    }
}
