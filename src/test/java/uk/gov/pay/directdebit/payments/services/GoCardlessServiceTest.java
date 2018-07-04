package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.BankAccountDetailsParser;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientFacade;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.util.Map;

import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture.aGoCardlessMandateFixture;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture.aGoCardlessPaymentFixture;

public abstract class GoCardlessServiceTest {
    static final String CUSTOMER_ID = "CU328471";
    static final String BANK_ACCOUNT_ID = "BA34983496";
    static final String MANDATE_ID = "sdkfhsdkjfhjdks";
    static final String TRANSACTION_ID = "sdkfhsd2jfhjdks";
    static final SortCode SORT_CODE = SortCode.of("123456");
    static final String ACCOUNT_NUMBER = "12345678";

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    PayerService mockedPayerService;
    @Mock
    TransactionService mockedTransactionService;
    @Mock
    GoCardlessClientFacade mockedGoCardlessClientFacade;
    @Mock
    GoCardlessCustomerDao mockedGoCardlessCustomerDao;
    @Mock
    GoCardlessMandateDao mockedGoCardlessMandateDao;
    @Mock
    GoCardlessPaymentDao mockedGoCardlessPaymentDao;
    @Mock
    MandateService mockedMandateService;
    @Mock
    GoCardlessEventDao mockedGoCardlessEventDao;
    @Mock
    MandateDao mockedMandateDao;
    @Mock
    BankAccountDetailsParser mockedBankAccountDetailsParser;

    GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    PayerFixture payerFixture = PayerFixture.aPayerFixture();
    GoCardlessCustomer goCardlessCustomer = new GoCardlessCustomer(null, payerFixture.getId(), CUSTOMER_ID, BANK_ACCOUNT_ID);
    MandateFixture mandateFixture = MandateFixture.aMandateFixture()
            .withPayerFixture(payerFixture)
            .withGatewayAccountFixture(gatewayAccountFixture)
            .withExternalId(MANDATE_ID);
    GoCardlessMandate goCardlessMandate = aGoCardlessMandateFixture()
            .withMandateId(mandateFixture.getId())
            .toEntity();
    Transaction transaction = TransactionFixture.aTransactionFixture()
            .withMandateFixture(mandateFixture)
            .withExternalId(TRANSACTION_ID)
            .toEntity();
    Map<String, String> confirmDetails = ImmutableMap.of(
            "sort_code", SORT_CODE.toString(),
            "account_number", ACCOUNT_NUMBER,
            "transaction_external_id", TRANSACTION_ID
    );
    GoCardlessPayment goCardlessPayment = aGoCardlessPaymentFixture().withTransactionId(transaction.getId()).toEntity();
    GoCardlessService service;
    ConfirmationDetails confirmationDetails;
}
