package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEventId;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class GoCardlessEventFixture implements DbFixture<GoCardlessEventFixture, GoCardlessEvent> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long eventId = RandomUtils.nextLong(1, 99999);
    private GoCardlessEventId goCardlessEventId = GoCardlessEventId.valueOf(RandomIdGenerator.newId());
    private String action = RandomStringUtils.randomAlphabetic(20);
    private GoCardlessResourceType resourceType = GoCardlessResourceType.PAYMENTS;
    private String resourceId = RandomStringUtils.randomAlphabetic(20);
    private String json = "{\"id\": \"somejson\"}";
    private String detailsCause = RandomStringUtils.randomAlphabetic(20);
    ;
    private String detailsDescription = RandomStringUtils.randomAlphabetic(20);
    private String detailsOrigin = RandomStringUtils.randomAlphabetic(20);
    private String detailsReasonCode = RandomStringUtils.randomAlphabetic(20);
    private String detailsScheme = RandomStringUtils.randomAlphabetic(20);
    private String linksMandate = RandomStringUtils.randomAlphabetic(20);
    private String linksNewCustomerBankAccount = RandomStringUtils.randomAlphabetic(20);
    private String linksNewMandate = RandomStringUtils.randomAlphabetic(20);
    private String linksOrganisation = RandomStringUtils.randomAlphabetic(20);
    private String linksParentEvent = RandomStringUtils.randomAlphabetic(20);
    private String linksPayment = RandomStringUtils.randomAlphabetic(20);
    private String linksPayout = RandomStringUtils.randomAlphabetic(20);
    private String linksPreviousCustomerBankAccount = RandomStringUtils.randomAlphabetic(20);
    private String linksRefund = RandomStringUtils.randomAlphabetic(20);
    private String linksSubscription = RandomStringUtils.randomAlphabetic(20);
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC);
    private GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf(RandomStringUtils.randomAlphanumeric(25));

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

    public GoCardlessEventFixture withOrganisationIdentifier(GoCardlessOrganisationId organisationIdentifier) {
        this.organisationIdentifier = organisationIdentifier;
        return this;
    }

    public String getDetailsCause() {
        return detailsCause;
    }

    public GoCardlessEventFixture setDetailsCause(String detailsCause) {
        this.detailsCause = detailsCause;
        return this;
    }

    public String getDetailsDescription() {
        return detailsDescription;
    }

    public GoCardlessEventFixture setDetailsDescription(String detailsDescription) {
        this.detailsDescription = detailsDescription;
        return this;
    }

    public String getDetailsOrigin() {
        return detailsOrigin;
    }

    public GoCardlessEventFixture setDetailsOrigin(String detailsOrigin) {
        this.detailsOrigin = detailsOrigin;
        return this;
    }

    public String getDetailsReasonCode() {
        return detailsReasonCode;
    }

    public GoCardlessEventFixture setDetailsReasonCode(String detailsReasonCode) {
        this.detailsReasonCode = detailsReasonCode;
        return this;
    }

    public String getDetailsScheme() {
        return detailsScheme;
    }

    public GoCardlessEventFixture setDetailsScheme(String detailsScheme) {
        this.detailsScheme = detailsScheme;
        return this;
    }

    public String getLinksMandate() {
        return linksMandate;
    }

    public GoCardlessEventFixture setLinksMandate(String linksMandate) {
        this.linksMandate = linksMandate;
        return this;
    }

    public String getLinksNewCustomerBankAccount() {
        return linksNewCustomerBankAccount;
    }

    public GoCardlessEventFixture setLinksNewCustomerBankAccount(String linksNewCustomerBankAccount) {
        this.linksNewCustomerBankAccount = linksNewCustomerBankAccount;
        return this;
    }

    public String getLinksNewMandate() {
        return linksNewMandate;
    }

    public GoCardlessEventFixture setLinksNewMandate(String linksNewMandate) {
        this.linksNewMandate = linksNewMandate;
        return this;
    }

    public String getLinksOrganisation() {
        return linksOrganisation;
    }

    public GoCardlessEventFixture setLinksOrganisation(String linksOrganisation) {
        this.linksOrganisation = linksOrganisation;
        return this;
    }

    public String getLinksParentEvent() {
        return linksParentEvent;
    }

    public GoCardlessEventFixture setLinksParentEvent(String linksParentEvent) {
        this.linksParentEvent = linksParentEvent;
        return this;
    }

    public String getLinksPayment() {
        return linksPayment;
    }

    public GoCardlessEventFixture setLinksPayment(String linksPayment) {
        this.linksPayment = linksPayment;
        return this;
    }

    public String getLinksPayout() {
        return linksPayout;
    }

    public GoCardlessEventFixture setLinksPayout(String linksPayout) {
        this.linksPayout = linksPayout;
        return this;
    }

    public String getLinksPreviousCustomerBankAccount() {
        return linksPreviousCustomerBankAccount;
    }

    public GoCardlessEventFixture setLinksPreviousCustomerBankAccount(String linksPreviousCustomerBankAccount) {
        this.linksPreviousCustomerBankAccount = linksPreviousCustomerBankAccount;
        return this;
    }

    public String getLinksRefund() {
        return linksRefund;
    }

    public GoCardlessEventFixture setLinksRefund(String linksRefund) {
        this.linksRefund = linksRefund;
        return this;
    }

    public String getLinksSubscription() {
        return linksSubscription;
    }

    public GoCardlessEventFixture setLinksSubscription(String linksSubscription) {
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
                        linksMandate,
                        linksNewCustomerBankAccount,
                        linksNewMandate,
                        linksOrganisation,
                        linksParentEvent,
                        linksPayment,
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
        return new GoCardlessEvent(id, eventId, goCardlessEventId, action, resourceType, json, detailsCause,
                detailsDescription, detailsOrigin, detailsReasonCode, detailsScheme, linksMandate, 
                linksNewCustomerBankAccount, linksNewMandate, linksOrganisation, linksParentEvent, linksPayment,
                linksPayout, linksPreviousCustomerBankAccount, linksRefund, linksSubscription, createdAt, 
                organisationIdentifier);
    }
}
