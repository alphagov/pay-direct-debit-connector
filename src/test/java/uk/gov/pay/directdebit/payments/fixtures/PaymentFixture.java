package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentId;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.payments.model.Payment.PaymentBuilder.aPayment;

public class PaymentFixture implements DbFixture<PaymentFixture, Payment> {

    private Long id = RandomUtils.nextLong(1, 99999);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture();
    private String externalId = RandomIdGenerator.newId();
    private Long amount = RandomUtils.nextLong(1, 99999);
    private PaymentState state = PaymentState.NEW;
    private String reference = RandomStringUtils.randomAlphanumeric(10);
    private String description = RandomStringUtils.randomAlphanumeric(20);
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);
    private PaymentProviderPaymentId paymentProviderId = SandboxPaymentId.valueOf(RandomStringUtils.randomAlphanumeric(20));
    private LocalDate chargeDate = LocalDate.now().plusDays(4);

    private PaymentFixture() {
    }

    public static PaymentFixture aPaymentFixture() {
        return new PaymentFixture();
    }

    public Long getId() {
        return id;
    }
    
    public PaymentFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public MandateFixture getMandateFixture() {
        return mandateFixture;
    }

    public PaymentFixture withMandateFixture(
            MandateFixture mandateFixture) {
        this.mandateFixture = mandateFixture;
        return this;
    }

    public String getExternalId() {
        return externalId;
    }

    public PaymentFixture withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public Long getAmount() {
        return amount;
    }

    public PaymentFixture withAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public PaymentState getState() {
        return state;
    }

    public PaymentFixture withState(PaymentState state) {
        this.state = state;
        return this;
    }

    public String getReference() {
        return reference;
    }

    public PaymentFixture withReference(String reference) {
        this.reference = reference;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PaymentFixture withDescription(String description) {
        this.description = description;
        return this;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public PaymentFixture withCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public PaymentFixture withPaymentProviderId(PaymentProviderPaymentId paymentProviderId) {
        this.paymentProviderId = paymentProviderId;
        return this;
    }

    public PaymentFixture withChargeDate(LocalDate chargeDate) {
        this.chargeDate = chargeDate;
        return this;
    }

    @Override
    public PaymentFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO" +
                                "    payments(\n" +
                                "        id,\n" +
                                "        mandate_id,\n" +
                                "        external_id,\n" +
                                "        amount,\n" +
                                "        state,\n" +
                                "        reference,\n" +
                                "        description,\n" +
                                "        created_date,\n" +
                                "        payment_provider_id,\n" +
                                "        charge_date\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\n",
                        id,
                        mandateFixture.getId(),
                        externalId,
                        amount,
                        state,
                        reference,
                        description,
                        createdDate,
                        paymentProviderId,
                        chargeDate
                )
        );
        return this;
    }

    @Override
    public Payment toEntity() {
        return aPayment()
                .withId(id)
                .withExternalId(externalId)
                .withAmount(amount)
                .withState(state)
                .withDescription(description)
                .withReference(reference)
                .withMandate(mandateFixture.toEntity())
                .withCreatedDate(createdDate)
                .withProviderId(paymentProviderId)
                .withChargeDate(chargeDate)
                .build();
    }

}
