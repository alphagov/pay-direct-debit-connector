package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class PaymentRequestFixture implements DbFixture<PaymentRequestFixture, PaymentRequest> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long gatewayAccountId = 23L;
    private String description = "Test description";
    private String externalId = RandomIdGenerator.newId();
    private long amount = 101L;
    private String returnUrl = "http://service.com/success-page";
    private String reference = "Test reference";
    private PayerFixture payerFixture = null;
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);

    private PaymentRequestFixture() {
    }

    public static PaymentRequestFixture aPaymentRequestFixture() {
        return new PaymentRequestFixture();
    }

    public PaymentRequestFixture withId(long id) {
        this.id = id;
        return this;
    }

    public PaymentRequestFixture withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public PaymentRequestFixture withReference(String reference) {
        this.reference = reference;
        return this;
    }

    public PaymentRequestFixture withReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
        return this;
    }

    public PaymentRequestFixture withGatewayAccountId(Long gatewayAccountId) {
        this.gatewayAccountId = gatewayAccountId;
        return this;
    }

    public PaymentRequestFixture withAmount(long amount) {
        this.amount = amount;
        return this;
    }

    public PaymentRequestFixture withCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public PaymentRequestFixture withPayerFixture(PayerFixture payer) {
        this.payerFixture = payer.withPaymentRequestId(id);
        return this;
    }

    public PaymentRequestFixture withDescription(String description) {
        this.description = description;
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public long getAmount() {
        return amount;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getReference() {
        return reference;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public String getDescription() {
        return description;
    }

    public Long getGatewayAccountId() {
        return gatewayAccountId;
    }

    public PayerFixture getPayerFixture() {
        return payerFixture;
    }

    @Override
    public PaymentRequestFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO" +
                                "    payment_requests(\n" +
                                "        id,\n" +
                                "        external_id,\n" +
                                "        amount,\n" +
                                "        gateway_account_id,\n" +
                                "        return_url,\n" +
                                "        description,\n" +
                                "        created_date,\n" +
                                "        reference\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?, ?, ?)\n",
                        id,
                        externalId,
                        amount,
                        gatewayAccountId,
                        returnUrl,
                        description,
                        Timestamp.from(createdDate.toInstant()),
                        reference
                )
        );
        if (payerFixture != null) {
            payerFixture.insert(jdbi);
        }
        return this;
    }

    @Override
    public PaymentRequest toEntity() {
        Payer payer = payerFixture != null ? payerFixture.toEntity() : null;
        return new PaymentRequest(id, amount, returnUrl, gatewayAccountId, description, reference, externalId, payer, createdDate);
    }

}
