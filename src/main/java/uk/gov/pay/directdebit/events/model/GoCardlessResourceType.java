package uk.gov.pay.directdebit.events.model;

public enum GoCardlessResourceType {

    PAYMENTS, MANDATES, PAYOUTS, REFUNDS, SUBSCRIPTIONS;

    public static GoCardlessResourceType fromString(String type) {
        for (GoCardlessResourceType typeEnum : GoCardlessResourceType.values()) {
            if (typeEnum.toString().equalsIgnoreCase(type)) {
                return typeEnum;
            }
        }
        throw new IllegalArgumentException("Unhandled GoCardless resource_type " + type);
    }

}
