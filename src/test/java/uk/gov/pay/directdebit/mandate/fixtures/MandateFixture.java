package uk.gov.pay.directdebit.mandate.fixtures;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateId;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.mandate.model.Mandate.MandateBuilder.aMandate;

public class MandateFixture implements DbFixture<MandateFixture, Mandate> {

    private Long id = RandomUtils.nextLong(1, 99999);
    private MandateExternalId mandateExternalId = MandateExternalId.valueOf(RandomIdGenerator.newId());
    private MandateBankStatementReference mandateReference = MandateBankStatementReference.valueOf(RandomStringUtils.randomAlphanumeric(18));
    private String serviceReference = RandomStringUtils.randomAlphanumeric(18);
    private MandateState state = MandateState.CREATED;
    private String stateDetails = null;
    private String stateDetailsDescription = null;
    private String returnUrl = "http://service.test/success-page";
    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    private PayerFixture payerFixture = null;
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);
    private PaymentProviderMandateId paymentProviderId;

    private MandateFixture() {
    }

    public static MandateFixture aMandateFixture() {
        return new MandateFixture();
    }

    public MandateFixture withGatewayAccountFixture(GatewayAccountFixture gatewayAccountFixture) {
        this.gatewayAccountFixture = gatewayAccountFixture;
        return this;
    }

    public MandateFixture withCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public MandateFixture withPayerFixture(
            PayerFixture payerFixture) {
        this.payerFixture = payerFixture;
        return this;
    }

    public Long getId() {
        return id;
    }

    public MandateFixture setId(Long id) {
        this.id = id;
        return this;
    }

    public MandateExternalId getExternalId() {
        return mandateExternalId;
    }

    public MandateFixture withExternalId(MandateExternalId externalId) {
        this.mandateExternalId = externalId;
        return this;
    }

    public MandateBankStatementReference getMandateReference() {
        return mandateReference;
    }

    public MandateFixture withMandateBankStatementReference(MandateBankStatementReference mandateReference) {
        this.mandateReference = mandateReference;
        return this;
    }

    public String getServiceReference() {
        return serviceReference;
    }

    public MandateFixture withServiceReference(String serviceReference) {
        this.serviceReference = serviceReference;
        return this;
    }

    public MandateState getState() {
        return state;
    }

    public MandateFixture withState(MandateState state) {
        this.state = state;
        return this;
    }

    public String getStateDetails() {
        return stateDetails;
    }

    public MandateFixture withStateDetails(String stateDetails) {
        this.stateDetailsDescription = stateDetails;
        return this;
    }

    public String getStateDetailsDescription() {
        return stateDetailsDescription;
    }

    public MandateFixture withStateDetailsDescription(String stateDetailsDescription) {
        this.stateDetailsDescription = stateDetailsDescription;
        return this;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public MandateFixture withReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
        return this;
    }

    public MandateFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public MandateFixture withPaymentProviderId(PaymentProviderMandateId paymentProviderId) {
        this.paymentProviderId = paymentProviderId;
        return this;
    }

    @Override
    public MandateFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO mandates (\n" +
                                "  id,\n" +
                                "  gateway_account_id,\n" +
                                "  external_id,\n" +
                                "  mandate_reference,\n" +
                                "  service_reference,\n" +
                                "  return_url,\n" +
                                "  state,\n" +
                                "  created_date,\n" +
                                "  payment_provider_id\n" +
                                ") VALUES (\n" +
                                "  ?, ?, ?, ?, ?, ?, ?, ?, ?\n" +
                                ")\n",
                        id,
                        gatewayAccountFixture.getId(),
                        mandateExternalId.toString(),
                        mandateReference.toString(),
                        serviceReference,
                        returnUrl,
                        state.toString(),
                        createdDate,
                        paymentProviderId
                )
        );
        if (payerFixture != null) {
            payerFixture.withMandateId(id);
            payerFixture.insert(jdbi);
        }
        return this;
    }

    @Override
    public Mandate toEntity() {
        Payer payer = payerFixture != null ? payerFixture.toEntity() : null;
        return aMandate()
                .withId(id)
                .withGatewayAccount(gatewayAccountFixture.toEntity())
                .withExternalId(mandateExternalId)
                .withMandateBankStatementReference(mandateReference)
                .withServiceReference(serviceReference)
                .withState(state)
                .withReturnUrl(returnUrl)
                .withCreatedDate(createdDate)
                .withPayer(payer)
                .withPaymentProviderId(paymentProviderId)
                .build();
    }
}
