package uk.gov.pay.directdebit.app.healthcheck;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PingTest {

    @Test
    public void testPing() {
        assertThat(new Ping().execute().isHealthy(), is(true));
    }
}
