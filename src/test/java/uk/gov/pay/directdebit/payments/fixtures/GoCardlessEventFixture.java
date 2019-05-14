package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class GoCardlessEventFixture implements DbFixture<GoCardlessEventFixture, GoCardlessEvent> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long eventId = RandomUtils.nextLong(1, 99999);
    private String goCardlessEventId = RandomIdGenerator.newId();
    private String action = RandomStringUtils.randomAlphabetic(20);
    private GoCardlessResourceType resourceType = GoCardlessResourceType.PAYMENTS;
    private String json = "{\"id\": \"somejson\"}";
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC);
    private PaymentProviderOrganisationIdentifier organisationIdentifier = PaymentProviderOrganisationIdentifier.of(RandomStringUtils.randomAlphanumeric(25));
    private String detailsCause = RandomStringUtils.randomAlphabetic(20);
    private String detailsDescription = RandomStringUtils.randomAlphabetic(20);
    private String detailsOrigin = RandomStringUtils.randomAlphabetic(20);
    private String detailsReasonCode = RandomStringUtils.randomAlphabetic(20);
    private String detailsScheme = RandomStringUtils.randomAlphabetic(20);
    private String mandateId = RandomStringUtils.randomAlphabetic(20);
    private String customerId = RandomStringUtils.randomAlphabetic(20);
    private String newMandateId = RandomStringUtils.randomAlphabetic(20);
    private String parentEventId = RandomStringUtils.randomAlphabetic(20);
    private String paymentId = RandomStringUtils.randomAlphabetic(20);
    private String payoutId = RandomStringUtils.randomAlphabetic(20);
    private String previousCustomerBankAccount = RandomStringUtils.randomAlphabetic(20);
    private String refundId = RandomStringUtils.randomAlphabetic(20);
    private String subscriptionId = RandomStringUtils.randomAlphabetic(20);
    
    public String getDetailsCause() {
        return detailsCause;
    }

    public GoCardlessEventFixture withDetailsCause(String detailsCause) {
        this.detailsCause = detailsCause;
        return this;
    }

    public String getDetailsDescription() {
        return detailsDescription;
    }

    public GoCardlessEventFixture withDetailsDescription(String detailsDescription) {
        this.detailsDescription = detailsDescription;
        return this;
    }

    public String getDetailsOrigin() {
        return detailsOrigin;
    }

    public GoCardlessEventFixture withDetailsOrigin(String detailsOrigin) {
        this.detailsOrigin = detailsOrigin;
        return this;
    }

    public String getDetailsReasonCode() {
        return detailsReasonCode;
    }

    public GoCardlessEventFixture withDetailsReasonCode(String detailsReasonCode) {
        this.detailsReasonCode = detailsReasonCode;
        return this;
    }

    public String getDetailsScheme() {
        return detailsScheme;
    }

    public GoCardlessEventFixture withDetailsScheme(String detailsScheme) {
        this.detailsScheme = detailsScheme;
        return this;
    }

    public String getMandateId() {
        return mandateId;
    }

    public GoCardlessEventFixture withMandateId(String mandateId) {
        this.mandateId = mandateId;
        return this;
    }

    public String getCustomerId() {
        return customerId;
    }

    public GoCardlessEventFixture withCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public String getNewMandateId() {
        return newMandateId;
    }

    public GoCardlessEventFixture withNewMandateId(String newMandateId) {
        this.newMandateId = newMandateId;
        return this;
    }

    public String getParentEventId() {
        return parentEventId;
    }

    public GoCardlessEventFixture withParentEventId(String parentEventId) {
        this.parentEventId = parentEventId;
        return this;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public GoCardlessEventFixture withPaymentId(String paymentId) {
        this.paymentId = paymentId;
        return this;
    }

    public String getPayoutId() {
        return payoutId;
    }

    public GoCardlessEventFixture withPayoutId(String payoutId) {
        this.payoutId = payoutId;
        return this;
    }

    public String getPreviousCustomerBankAccount() {
        return previousCustomerBankAccount;
    }

    public GoCardlessEventFixture withPreviousCustomerBankAccount(String previousCustomerBankAccount) {
        this.previousCustomerBankAccount = previousCustomerBankAccount;
        return this;
    }

    public String getRefundId() {
        return refundId;
    }

    public GoCardlessEventFixture withRefundId(String refundId) {
        this.refundId = refundId;
        return this;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public GoCardlessEventFixture withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    private GoCardlessEventFixture() {
    }

    public static GoCardlessEventFixture aGoCardlessEventFixture() {
        return new GoCardlessEventFixture();
    }

    public Long getId() {
        return id;
    }

    public GoCardlessEventFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public Long getEventId() {
        return eventId;
    }

    public GoCardlessEventFixture withEventId(Long eventId) {
        this.eventId = eventId;
        return this;
    }

    public String getJson() {
        return json;
    }

    public GoCardlessEventFixture withJson(String json) {
        this.json = json;
        return this;
    }

    public String getGoCardlessEventId() {
        return goCardlessEventId;
    }

    public GoCardlessEventFixture withGoCardlessEventId(String eventId) {
        this.goCardlessEventId = eventId;
        return this;
    }

    public String getAction() {
        return action;
    }

    public GoCardlessEventFixture withAction(String action) {
        this.action = action;
        return this;
    }

    public GoCardlessResourceType getResourceType() {
        return resourceType;
    }

    public GoCardlessEventFixture withResourceType(GoCardlessResourceType resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public GoCardlessEventFixture withCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public GoCardlessEventFixture withOrganisationIdentifier(PaymentProviderOrganisationIdentifier organisationIdentifier) {
        this.organisationIdentifier = organisationIdentifier;
        return this;
    }

    @Override
    public GoCardlessEventFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO" +
                                "    gocardless_events(\n" +
                                "        id,\n" +
                                "        internal_event_id,\n" +
                                "        event_id,\n" +
                                "        action,\n" +
                                "        resource_type,\n" +
                                "        json,\n" +
                                "        created_at\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?, ?)\n",
                        id,
                        eventId,
                        goCardlessEventId,
                        action,
                        resourceType.toString(),
                        json,
                        Timestamp.from(createdAt.toInstant())
                )
        );
        return this;
    }

    @Override
    public GoCardlessEvent toEntity() {
        return GoCardlessEvent.GoCardlessEventBuilder.aGoCardlessEvent()
                .withGoCardlessEventId(goCardlessEventId)
                .withAction(action)
                .withResourceType(resourceType)
                .withJson(json)
                .withCreatedAt(createdAt)
                .withOrganisationIdentifier(organisationIdentifier)
                .build();
    }

}
