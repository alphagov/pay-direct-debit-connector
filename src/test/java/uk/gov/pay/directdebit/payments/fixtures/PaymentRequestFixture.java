package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomUtils;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PaymentRequestFixture implements DbFixture<PaymentRequestFixture, PaymentRequest> {
    private DatabaseTestHelper databaseTestHelper;
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long gatewayAccountId = 23L;
    private String description = "Test description";
    private String externalId = RandomIdGenerator.newId();
    private long amount = 101L;
    private String returnUrl = "http://service.com/success-page";
    private String reference = "Test reference";

    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneId.of("UTC"));

    public PaymentRequestFixture(DatabaseTestHelper databaseTestHelper) {
        this.databaseTestHelper = databaseTestHelper;
    }

    public static PaymentRequestFixture paymentRequestFixture(DatabaseTestHelper databaseHelper) {
        return new PaymentRequestFixture(databaseHelper);
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

    @Override
    public PaymentRequestFixture insert() {
        databaseTestHelper.add(this);
        return this;
    }

    @Override
    public PaymentRequest toEntity() {
        return new PaymentRequest(id, amount, returnUrl, gatewayAccountId, description, reference, externalId, createdDate);
    }

}
