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
import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;
import uk.gov.pay.directdebit.payments.api.ExternalPaymentStateWithDetails;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.api.PaymentViewResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.Payment;
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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.api.PaymentResponse.PaymentResponseBuilder.aPaymentResponse;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;

@RunWith(MockitoJUnitRunner.class)
public class PaymentViewServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private String gatewayAccountExternalId = RandomIdGenerator.newId();
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);
    private List<PaymentResponse> paymentViewList = new ArrayList<>();
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
            PaymentResponse paymentResponse = aPaymentResponse()
                    .withCreatedDate(createdDate)
                    .withState(new ExternalPaymentStateWithDetails(ExternalPaymentState.EXTERNAL_PENDING, "example_details"))
                    .withReference("Pay reference " + i)
                    .withAmount(1000L + i)
                    .withDescription("This is a description " + i)
                    .withTransactionExternalId(RandomIdGenerator.newId())
                    .build();
            paymentViewList.add(paymentResponse);
        }
        gatewayAccount = new GatewayAccount(1L, gatewayAccountExternalId, null, null, null, null);
        when(mockUriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri("http://app.com"),
                UriBuilder.fromUri("http://app.com"));
        when(mockUriInfo.getPath()).thenReturn("/v1/api/accounts/" + gatewayAccount.getExternalId() + "/transactions/view");
        try {
            URI uri = new URI("http://app.com");
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
        
        when(gatewayAccountDao.findByExternalId(gatewayAccountExternalId)).thenReturn(Optional.of(gatewayAccount));
        when(paymentViewDao.searchPaymentView(any(PaymentViewSearchParams.class))).thenReturn(paymentViewList);
        when(paymentViewDao.getPaymentViewCount(any(PaymentViewSearchParams.class))).thenReturn(4L);
        PaymentViewResponse response = paymentViewService.getPaymentViewResponse(searchParams);
        assertThat(response.getPaymentViewResponses().get(3).getAmount(), is(1003L));
        assertThat(response.getPaymentViewResponses().get(0).getState(),
                is(new ExternalPaymentStateWithDetails(ExternalPaymentState.EXTERNAL_PENDING, "example_details")));
        assertThat(response.getPaymentViewResponses().get(2).getCreatedDate(), is(createdDate));
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
        List<PaymentResponse> paymentViewList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Payment payment = aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withAmount(((long) 100 + i))
                    .withReference("MBK" + i)
                    .withDescription("Description" + i)
                    .withCreatedDate(createdDate)
                    .toEntity();
            paymentViewList.add(aPaymentResponse()
                    .withTransactionExternalId(payment.getExternalId())
                    .withAmount(payment.getAmount())
                    .withReference(payment.getReference())
                    .withDescription(payment.getDescription())
                    .withCreatedDate(payment.getCreatedDate())
                    .build());
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
        assertThat(paymentViewResponse.getPaginationBuilder().getFirstLink().getHref().contains("?page=1"), is(true));
        assertThat(paymentViewResponse.getPaginationBuilder().getLastLink().getHref().contains("?page=3"), is(true));
        assertThat(paymentViewResponse.getPaginationBuilder().getPrevLink().getHref().contains("?page=1"), is(true));
        assertThat(paymentViewResponse.getPaginationBuilder().getNextLink().getHref().contains("?page=3"), is(true));
        assertThat(paymentViewResponse.getPaginationBuilder().getSelfLink().getHref().contains("?page=2"), is(true));
    }

    @Test
    public void shouldReturnNoRecords_whenPaginationSetToPage2AndNoDisplaySize() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().withExternalId(gatewayAccountExternalId);
        GatewayAccount testGatewayAccount = gatewayAccountFixture.toEntity();
        List<PaymentResponse> paymentViewList = new ArrayList<>();
        when(gatewayAccountDao.findByExternalId(gatewayAccountExternalId)).thenReturn(Optional.of(testGatewayAccount));
        when(paymentViewDao.getPaymentViewCount(any(PaymentViewSearchParams.class))).thenReturn(18L);
        when(paymentViewDao.searchPaymentView(any(PaymentViewSearchParams.class))).thenReturn(paymentViewList);
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountExternalId)
                .withPage(2L);
        PaymentViewResponse paymentViewResponse = paymentViewService.getPaymentViewResponse(searchParams);
        assertThat(paymentViewResponse.getCount(), is(0L));
        assertThat(paymentViewResponse.getPage(), is(2L));
        assertThat(paymentViewResponse.getTotal(), is(18L));
        assertThat(paymentViewResponse.getPaymentViewResponses().size(), is(0));
        assertThat(paymentViewResponse.getPaginationBuilder().getFirstLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(paymentViewResponse.getPaginationBuilder().getLastLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(paymentViewResponse.getPaginationBuilder().getPrevLink().getHref().contains("?page=1&display_size=500"), is(true));
        assertThat(paymentViewResponse.getPaginationBuilder().getNextLink(), is(nullValue()));
        assertThat(paymentViewResponse.getPaginationBuilder().getSelfLink().getHref().contains("?page=2&display_size=500"), is(true));
    }
}
