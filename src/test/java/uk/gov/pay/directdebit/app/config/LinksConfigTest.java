package uk.gov.pay.directdebit.app.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LinksConfigTest {
    @Spy
    private LinksConfig linksConfig = new LinksConfig();

    @Test
    public void shouldBuildTheCorrectDDGuaranteeUrl() {
        when(linksConfig.getFrontendUrl()).thenReturn("https://frontend.url");
        assertThat(linksConfig.getDirectDebitGuaranteeUrl(), is("https://frontend.url/direct-debit-guarantee"));
    }
}
