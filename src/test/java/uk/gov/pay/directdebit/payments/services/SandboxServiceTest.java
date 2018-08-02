package uk.gov.pay.directdebit.payments.services;

import org.exparity.hamcrest.date.LocalDateMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.mandate.model.OneOffConfirmationDetails;
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
    public void confirmOnDemand_shouldNotDoAnything() {
        MandateFixture mandateFixture = aMandateFixture().withMandateType(MandateType.ONE_OFF).withGatewayAccountFixture(gatewayAccountFixture);
        BankAccountDetails bankAccountDetails = new BankAccountDetails(AccountNumber.of("12345678"), SortCode.of("123456"));
        Mandate mandate = service
                .confirmOnDemandMandate(mandateFixture.toEntity(), bankAccountDetails);
        assertThat(mandate, is(mandateFixture.toEntity()));
    }

    @Test
    public void confirmOneOff_shouldCreateConfirmationDetails() {
        MandateFixture mandateFixture = aMandateFixture().withMandateType(MandateType.ONE_OFF).withGatewayAccountFixture(gatewayAccountFixture);
        BankAccountDetails bankAccountDetails = new BankAccountDetails(AccountNumber.of("12345678"), SortCode.of("123456"));
        TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture);

        OneOffConfirmationDetails confirmationDetails = service
                .confirmOneOffMandate(mandateFixture.toEntity(), bankAccountDetails, transactionFixture.toEntity());

        assertThat(confirmationDetails.getMandate(), is(mandateFixture.toEntity()));
        assertThat(confirmationDetails.getChargeDate(), is(LocalDateMatchers
                .within(1, ChronoUnit.DAYS, LocalDate.now().plusDays(4))));
    }


    @Test
    public void collect_shouldReturnCollectionDate() {
        Mandate mandate = aMandateFixture().withMandateType(MandateType.ONE_OFF).withGatewayAccountFixture(gatewayAccountFixture).toEntity();
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
