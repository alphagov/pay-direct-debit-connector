package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEventId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static uk.gov.pay.directdebit.payments.model.GoCardlessEvent.GoCardlessEventBuilder.aGoCardlessEvent;

public class GoCardlessEventFixture implements DbFixture<GoCardlessEventFixture, GoCardlessEvent> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long eventId = RandomUtils.nextLong(1, 99999);
    private GoCardlessEventId goCardlessEventId = GoCardlessEventId.valueOf(RandomIdGenerator.newId());
    private String action = randomAlphabetic(20);
    private GoCardlessResourceType resourceType = GoCardlessResourceType.PAYMENTS;
    private String resourceId = randomAlphabetic(20);
    private String json = "{\"id\": \"somejson\"}";
    private String detailsCause = randomAlphabetic(20);
    private String detailsDescription = randomAlphabetic(20);
    private String detailsOrigin = randomAlphabetic(20);
    private String detailsReasonCode = randomAlphabetic(20);
    private String detailsScheme = randomAlphabetic(20);
    private GoCardlessMandateId linksMandate = GoCardlessMandateId.valueOf(randomAlphabetic(20));
    private String linksNewCustomerBankAccount = randomAlphabetic(20);
    private GoCardlessMandateId linksNewMandate = GoCardlessMandateId.valueOf(randomAlphabetic(20));
    private GoCardlessOrganisationId linksOrganisation = GoCardlessOrganisationId.valueOf(randomAlphanumeric(20));
    private String linksParentEvent = randomAlphabetic(20);
    private GoCardlessPaymentId linksPayment = GoCardlessPaymentId.valueOf(randomAlphabetic(20));
    private String linksPayout = randomAlphabetic(20);
    private String linksPreviousCustomerBankAccount = randomAlphabetic(20);
    private String linksRefund = randomAlphabetic(20);
    private String linksSubscription = randomAlphabetic(20);
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC);

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

    public GoCardlessEventId getGoCardlessEventId() {
        return goCardlessEventId;
    }

    public GoCardlessEventFixture withGoCardlessEventId(GoCardlessEventId eventId) {
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

    public String getResourceId() {
        return resourceId;
    }

    public GoCardlessEventFixture withResourceId(String resourceId) {
        this.resourceId = resourceId;
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

    public GoCardlessMandateId getLinksMandate() {
        return linksMandate;
    }

    public GoCardlessEventFixture withLinksMandate(GoCardlessMandateId linksMandate) {
        this.linksMandate = linksMandate;
        return this;
    }

    public String getLinksNewCustomerBankAccount() {
        return linksNewCustomerBankAccount;
    }

    public GoCardlessEventFixture withLinksNewCustomerBankAccount(String linksNewCustomerBankAccount) {
        this.linksNewCustomerBankAccount = linksNewCustomerBankAccount;
        return this;
    }

    public GoCardlessMandateId getLinksNewMandate() {
        return linksNewMandate;
    }

    public GoCardlessEventFixture withLinksNewMandate(GoCardlessMandateId linksNewMandate) {
        this.linksNewMandate = linksNewMandate;
        return this;
    }

    public GoCardlessOrganisationId getLinksOrganisation() {
        return linksOrganisation;
    }

    public GoCardlessEventFixture withLinksOrganisation(GoCardlessOrganisationId linksOrganisation) {
        this.linksOrganisation = linksOrganisation;
        return this;
    }

    public String getLinksParentEvent() {
        return linksParentEvent;
    }

    public GoCardlessEventFixture withLinksParentEvent(String linksParentEvent) {
        this.linksParentEvent = linksParentEvent;
        return this;
    }

    public GoCardlessPaymentId getLinksPayment() {
        return linksPayment;
    }

    public GoCardlessEventFixture withLinksPayment(GoCardlessPaymentId linksPayment) {
        this.linksPayment = linksPayment;
        return this;
    }

    public String getLinksPayout() {
        return linksPayout;
    }

    public GoCardlessEventFixture withLinksPayout(String linksPayout) {
        this.linksPayout = linksPayout;
        return this;
    }

    public String getLinksPreviousCustomerBankAccount() {
        return linksPreviousCustomerBankAccount;
    }

    public GoCardlessEventFixture withLinksPreviousCustomerBankAccount(String linksPreviousCustomerBankAccount) {
        this.linksPreviousCustomerBankAccount = linksPreviousCustomerBankAccount;
        return this;
    }

    public String getLinksRefund() {
        return linksRefund;
    }

    public GoCardlessEventFixture withLinksRefund(String linksRefund) {
        this.linksRefund = linksRefund;
        return this;
    }

    public String getLinksSubscription() {
        return linksSubscription;
    }

    public GoCardlessEventFixture withLinksSubscription(String linksSubscription) {
        this.linksSubscription = linksSubscription;
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
                                "        created_at,\n" +
                                "        details_cause, \n" +
                                "        details_description, \n" +
                                "        details_origin, \n" +
                                "        details_reason_code, \n" +
                                "        details_scheme, \n" +
                                "        links_mandate, \n" +
                                "        links_new_customer_bank_account, \n" +
                                "        links_new_mandate, \n" +
                                "        links_organisation, \n" +
                                "        links_parent_event, \n" +
                                "        links_payment, \n" +
                                "        links_payout, \n" +
                                "        links_previous_customer_bank_account, \n" +
                                "        links_refund, \n" +
                                "        links_subscription\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\n",
                        id,
                        eventId,
                        goCardlessEventId.toString(),
                        action,
                        resourceType.toString(),
                        json,
                        detailsCause,
                        detailsDescription,
                        detailsOrigin,
                        detailsReasonCode,
                        detailsScheme,
                        linksMandate.toString(),
                        linksNewCustomerBankAccount,
                        linksNewMandate,
                        linksOrganisation.toString(),
                        linksParentEvent,
                        linksPayment.toString(),
                        linksPayout,
                        linksPreviousCustomerBankAccount,
                        linksRefund,
                        linksSubscription,
                        Timestamp.from(createdAt.toInstant())
                )
        );
        return this;
    }

    @Override
    public GoCardlessEvent toEntity() {
        return aGoCardlessEvent()
                .withId(id)
                .withInternalEventId(eventId)
                .withGoCardlessEventId(goCardlessEventId)
                .withAction(action)
                .withResourceId(resourceId)
                .withResourceType(resourceType)
                .withJson(json)
                .withDetailsCause(detailsCause)
                .withDetailsDescription(detailsDescription)
                .withDetailsOrigin(detailsOrigin)
                .withDetailsReasonCode(detailsReasonCode)
                .withDetailsScheme(detailsScheme)
                .withLinksMandate(linksMandate)
                .withLinksNewCustomerBankAccount(linksNewCustomerBankAccount)
                .withLinksNewMandate(linksNewMandate)
                .withLinksOrganisation(linksOrganisation)
                .withLinksParentEvent(linksParentEvent)
                .withLinksPayment(linksPayment)
                .withLinksPayout(linksPayout)
                .withLinksPreviousCustomerBankAccount(linksPreviousCustomerBankAccount)
                .withLinksRefund(linksRefund).withLinksSubscription(linksSubscription)
                .withCreatedAt(createdAt)
                .build();
    }
}
