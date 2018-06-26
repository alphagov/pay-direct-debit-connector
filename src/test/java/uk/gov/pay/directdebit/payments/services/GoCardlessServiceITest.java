package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.services.MandateConfirmService;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.BankAccountDetailsParser;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientFacade;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.exception.CreatePaymentFailedException;
import uk.gov.pay.directdebit.payments.fixtures.ConfirmationDetailsFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GoCardlessServiceITest {
    private static final String TRANSACTION_ID = "sdkfhsd2jfhjdks";
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @DropwizardTestContext
    private TestContext testContext;

    private GoCardlessCustomerDao goCardlessCustomerDao;
    private GoCardlessMandateDao goCardlessMandateDao;
    private GoCardlessPaymentDao goCardlessPaymentDao;
    private GoCardlessEventDao goCardlessEventDao;

    private MandateDao mandateDao;

    @Mock
    private GoCardlessClientFacade goCardlessClientFacade;
    @Mock
    private MandateConfirmService mockedMandateConfirmService;
    @Mock
    private PayerService mockedPayerService;
    @Mock
    private TransactionService mockedTransactionService;
    @Mock
    private BankAccountDetailsParser mockedBankAccountDetailsParser;

    private GoCardlessService service;
    private GatewayAccountFixture testGatewayAccount;
    private Map<String, String> confirmDetails = ImmutableMap
            .of("sort_code", "123456", "account_number", "12345678", "transaction_external_id", TRANSACTION_ID);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        mandateDao = testContext.getJdbi().onDemand(MandateDao.class);
        goCardlessCustomerDao = testContext.getJdbi().onDemand(GoCardlessCustomerDao.class);
        goCardlessMandateDao = testContext.getJdbi().onDemand(GoCardlessMandateDao.class);
        goCardlessPaymentDao = testContext.getJdbi().onDemand(GoCardlessPaymentDao.class);
        goCardlessEventDao = testContext.getJdbi().onDemand(GoCardlessEventDao.class);


        service = new GoCardlessService(mockedPayerService, mockedTransactionService,
                mockedMandateConfirmService, goCardlessClientFacade,
                goCardlessCustomerDao, goCardlessPaymentDao, goCardlessMandateDao, goCardlessEventDao,
                mandateDao, mockedBankAccountDetailsParser);

        testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
    }
    
    @Test
    public void shouldNotPersistMandate_whenGCCallToCreatePaymentFails() throws Exception {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount);

        String accountName = "account_name";
        String sortCode = "sort_code";
        String accountNumber = "account_number";
        long mandateId = 123456L;
        
        PayerFixture payerFixture = PayerFixture.aPayerFixture()
                .withMandateId(mandateFixture.getId())
                .withName(accountName)
                .withSortCode(sortCode)
                .withAccountNumber(accountNumber)
                .withId(mandateId);

        GoCardlessCustomer goCardlessCustomer = GoCardlessCustomerFixture.aGoCardlessCustomerFixture()
                .withPayerId(mandateId)
                .toEntity();
        
        mandateFixture.withPayerFixture(payerFixture).insert(testContext.getJdbi());

        ConfirmationDetails confirmationDetails = ConfirmationDetailsFixture.confirmationDetails()
                .withMandateFixture(mandateFixture)
                .withAccountNumber(accountNumber)
                .withSortCode(sortCode)
                .withTransactionFixture(TransactionFixture.aTransactionFixture())
                .build();

        GoCardlessMandate goCardlessMandate = GoCardlessMandateFixture.aGoCardlessMandateFixture()
                .withMandateId(mandateFixture.getId())
                .withGoCardlessMandateId("abc")
                .toEntity();
        
        when(mockedMandateConfirmService.confirm(mandateFixture.getExternalId(), confirmDetails))
                .thenReturn(confirmationDetails);
        when(goCardlessClientFacade.createCustomer(mandateFixture.getExternalId(), payerFixture.toEntity()))
                .thenReturn(goCardlessCustomer);
        when(goCardlessClientFacade.createCustomerBankAccount(mandateFixture.getExternalId(), goCardlessCustomer, payerFixture.getName(), sortCode, accountNumber))
                .thenReturn(goCardlessCustomer);
        when(goCardlessClientFacade.createMandate(mandateFixture.toEntity(), goCardlessCustomer))
                .thenReturn(goCardlessMandate);
        when(goCardlessClientFacade.createPayment(any(), any())).thenThrow(new RuntimeException());

        try {
            service.confirm(mandateFixture.getExternalId(), testGatewayAccount.toEntity(), confirmDetails);
        } catch (Exception e) {
            // do nuttin'
        }
        
        assertNull(testContext.getDatabaseTestHelper().getGoCardlessMandateById(goCardlessMandate.getId()));
    }
}
