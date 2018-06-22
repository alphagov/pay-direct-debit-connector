package uk.gov.pay.directdebit.payments.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.api.PaymentViewListResponse;
import uk.gov.pay.directdebit.payments.api.PaymentViewResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentView;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)
public class PaymentViewServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private String gatewayAccountExternalId = RandomIdGenerator.newId();
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);
    private List<PaymentView> paymentViewList = new ArrayList<>();
    @Mock
    private PaymentViewDao paymentViewDao;
    @Mock
    private GatewayAccountDao gatewayAccountDao;
    @Mock
    UriInfo mockUriInfo;
    private PaymentViewService paymentViewService;
    private GatewayAccount gatewayAccount;

    @Before
    public void setUp() {
        for (int i = 0; i < 4; i++) {
            PaymentView paymentView = new PaymentView(
                    gatewayAccountExternalId,
                    RandomIdGenerator.newId(),
                    1000L + i,
                    "Pay reference" + i,
                    "This is a description" + i,
                    createdDate,
                    "John Doe" + i,
                    "doe@mail.mail",
                    PaymentState.NEW);
            paymentViewList.add(paymentView);
        }
        gatewayAccount = new GatewayAccount(1L, gatewayAccountExternalId, null, null, null, null, null);
        when(mockUriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri("http://app.com"),
                UriBuilder.fromUri("http://app.com"));
        when(mockUriInfo.getPath()).thenReturn("/v1/api/accounts/" + gatewayAccount.getExternalId() + "/transactions/view");
        try {
            URI uri = new URI("http://app.com");
            when(mockUriInfo.getBaseUri()).thenReturn(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        paymentViewService = new PaymentViewService(paymentViewDao, gatewayAccountDao)
                                    .withUriInfo(mockUriInfo);
    }

    @Test
    public void getPaymentViewList_withGatewayAccountIdAndOffsetAndLimit_shouldPopulateResponse() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountExternalId)
            .withPage(1L)
            .withDisplaySize(100L)
            .withFromDateString(createdDate.toString())
            .withToDateString(createdDate.toString());    
        List<PaymentViewListResponse> listResponses = new ArrayList<>();
        for (PaymentView paymentView : paymentViewList){
            listResponses.add(new PaymentViewListResponse(paymentView.getTransactionExternalId(),
                    paymentView.getAmount(), paymentView.getReference(), paymentView.getDescription(),
                    paymentView.getCreatedDate().toString(), paymentView.getName(), paymentView.getEmail(), paymentView.getState().toExternal()));
        }
        when(gatewayAccountDao.findByExternalId(gatewayAccountExternalId)).thenReturn(Optional.of(gatewayAccount));
        when(paymentViewDao.searchPaymentView(any(PaymentViewSearchParams.class))).thenReturn(paymentViewList);
        when(paymentViewDao.getPaymentViewCount(any(PaymentViewSearchParams.class))).thenReturn(4L);
        PaymentViewResponse response = paymentViewService.getPaymentViewResponse(searchParams);
        assertThat(response.getPaymentViewResponses().get(3).getAmount(), is(1003L));
        assertThat(response.getPaymentViewResponses().get(1).getName(), is("John Doe1"));
        assertThat(response.getPaymentViewResponses().get(0).getState(), is((PaymentState.NEW.toExternal())));
        assertThat(response.getPaymentViewResponses().get(2).getCreatedDate(), is(createdDate.toString()));
    }

    @Test
    public void shouldThrowGatewayAccountNotFoundException_whenGatewayNotExists() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountExternalId)
                .withPage(10L)
                .withDisplaySize(100L)
                .withFromDateString(createdDate.toString())
                .withToDateString(createdDate.toString());
        thrown.expect(GatewayAccountNotFoundException.class);
        thrown.expectMessage("Unknown gateway account: " + gatewayAccountExternalId);
        when(gatewayAccountDao.findByExternalId(gatewayAccountExternalId)).thenReturn(Optional.empty());
        paymentViewService.getPaymentViewResponse(searchParams);
    }

    @Test
    public void shouldReturn20Records_whenPaginationSetToPage2AndDisplaySize20() {
        ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1L);
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().withExternalId(gatewayAccountExternalId);
        MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);
        GatewayAccount testGatewayAccount = gatewayAccountFixture.toEntity();
        Mandate mandate = mandateFixture.toEntity();
        Payer payer = aPayerFixture().withMandateId(mandate.getId())
                .withName("J. Doe")
                .withEmail("j.doe@mail.fake")
                .toEntity();
        List<PaymentView> paymentViewList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Transaction transaction = aTransactionFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withAmount(((long) 100 + i))
                    .withReference("MBK" + i)
                    .withDescription("Description" + i)
                    .withCreatedDate(createdDate)
                    .toEntity();
            paymentViewList.add(new PaymentView(gatewayAccountExternalId, 
                    transaction.getExternalId(),
                    transaction.getAmount(), 
                    transaction.getReference(), 
                    transaction.getDescription(),
                    transaction.getCreatedDate(), 
                    payer.getName(), 
                    payer.getEmail(),
                    transaction.getState()));
        }
        when(gatewayAccountDao.findByExternalId(gatewayAccountExternalId)).thenReturn(Optional.of(testGatewayAccount));
        when(paymentViewDao.getPaymentViewCount(any(PaymentViewSearchParams.class))).thenReturn(50L);
        when(paymentViewDao.searchPaymentView(any(PaymentViewSearchParams.class))).thenReturn(paymentViewList);
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountExternalId)
                .withPage(2L)
                .withDisplaySize(20L);
        PaymentViewResponse paymentViewResponse = paymentViewService.getPaymentViewResponse(searchParams);
        assertThat(paymentViewResponse.getCount(), is(20L));
        assertThat(paymentViewResponse.getPage(), is(2L));
        assertThat(paymentViewResponse.getTotal(), is(50L));
        assertThat(paymentViewResponse.getPaymentViewResponses().get(0).getLinks().get(0).getRel(), is("self"));
        assertThat(paymentViewResponse.getPaginationBuilder().getFirstLink().getHref().contains("&page_number=1"), is(true));
        assertThat(paymentViewResponse.getPaginationBuilder().getLastLink().getHref().contains("&page_number=3"), is(true));
        assertThat(paymentViewResponse.getPaginationBuilder().getPrevLink().getHref().contains("&page_number=1"), is(true));
        assertThat(paymentViewResponse.getPaginationBuilder().getNextLink().getHref().contains("&page_number=3"), is(true));
        assertThat(paymentViewResponse.getPaginationBuilder().getSelfLink().getHref().contains("&page_number=2"), is(true));
    }
}
