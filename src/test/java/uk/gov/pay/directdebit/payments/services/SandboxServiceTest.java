package uk.gov.pay.directdebit.payments.services;

import org.exparity.hamcrest.date.LocalDateMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class SandboxServiceTest {


    private SandboxService service;

    private GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture();
    private MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);

    @Before
    public void setUp() {
        service = new SandboxService();
    }

    @Test
    public void confirmMandate_shouldReturnPaymentProviderIdAndBankStatementReference() {
        MandateFixture mandateFixture = aMandateFixture()
                .withExternalId(MandateExternalId.valueOf("anExternalId"))
                .withMandateBankStatementReference(MandateBankStatementReference.valueOf("aBankReference"))
                .withGatewayAccountFixture(gatewayAccountFixture);
        BankAccountDetails bankAccountDetails = new BankAccountDetails(AccountNumber.of("12345678"), SortCode.of("123456"));

        var confirmMandateResponse = service.confirmMandate(mandateFixture.toEntity(), bankAccountDetails);

        assertThat(confirmMandateResponse.getPaymentProviderMandateId().toString(), is(mandateFixture.getExternalId().toString()));
        assertThat(confirmMandateResponse.getMandateBankStatementReference(), is(mandateFixture.getMandateReference()));
    }

    @Test
    public void collect_shouldReturnCollectionDate() {
        Mandate mandate = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).toEntity();
        Transaction transaction = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture).toEntity();

        LocalDate chargeDate = service.collect(mandate, transaction);

        assertThat(chargeDate, is(LocalDateMatchers
                .within(1, ChronoUnit.DAYS, LocalDate.now().plusDays(4))));
    }

    @Test
    public void shouldValidateBankAccountDetails() {
        BankAccountDetails bankAccountDetails = new BankAccountDetails(AccountNumber.of("12345678"), SortCode
                .of("123456"));

        BankAccountValidationResponse response = service.validate(aMandateFixture().toEntity(), bankAccountDetails);
        assertThat(response.isValid(), is(true));
        assertThat(response.getBankName(), is("Sandbox Bank"));
    }

    @Test
    public void shouldReturnSunName() {
        Optional<SunName> result = service.getSunName(mandateFixture.toEntity());

        assertThat(result, is(Optional.of(SunName.of("Sandbox SUN Name"))));
    }

}
