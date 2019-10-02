package uk.gov.pay.directdebit.pact;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;
import org.junit.runners.Suite;
import uk.gov.pay.commons.testing.pact.provider.CreateTestSuite;

import java.util.Map;

@RunWith(AllTests.class)
public class ContractTestSuite {

    public static TestSuite suite() {
        Map<String, JUnit4TestAdapter> map = Map.of(
                "publicapi", new JUnit4TestAdapter(PublicApiContractTest.class),
                "selfservice", new JUnit4TestAdapter(SelfServiceContractTest.class));
        return CreateTestSuite.create(map);
    }
}
