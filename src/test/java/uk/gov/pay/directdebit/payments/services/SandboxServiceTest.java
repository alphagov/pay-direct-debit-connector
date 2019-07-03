package uk.gov.pay.directdebit.payments.services;

import org.exparity.hamcrest.date.LocalDateMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentIdAndChargeDate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class SandboxServiceTest {
    
    private SandboxService service;

    private GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture();
    private MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);

    @Mock
    protected PaymentDao paymentDao;

    @Before
    public void setUp() {
        service = new SandboxService(paymentDao);
    }

    @Test
    public void confirmMandate_shouldReturnPaymentProviderIdAndBankStatementReference() {
        MandateFixture mandateFixture = aMandateFixture()
                .withExternalId(MandateExternalId.valueOf("anExternalId"))
                .withMandateBankStatementReference(null)
                .withGatewayAccountFixture(gatewayAccountFixture);
        BankAccountDetails bankAccountDetails = new BankAccountDetails(AccountNumber.of("12345678"), SortCode.of("123456"));

        var confirmMandateResponse = service.confirmMandate(mandateFixture.toEntity(), bankAccountDetails);

        assertThat(confirmMandateResponse.getPaymentProviderMandateId().toString(), is(mandateFixture.getExternalId().toString()));
        assertThat(confirmMandateResponse.getMandateBankStatementReference(), is(notNullValue()));
    }

    @Test
    public void collect_shouldReturnCollectionDate() {
        Mandate mandate = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).toEntity();
        Payment payment = PaymentFixture.aPaymentFixture().withMandateFixture(mandateFixture).toEntity();

        PaymentProviderPaymentIdAndChargeDate providerPaymentIdAndChargeDate = service.collect(mandate, payment);

        assertThat(providerPaymentIdAndChargeDate.getChargeDate(), is(LocalDateMatchers
                .within(1, ChronoUnit.DAYS, LocalDate.now().plusDays(4))));
        
        verify(paymentDao).updateProviderIdAndChargeDate(any(Payment.class));
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
