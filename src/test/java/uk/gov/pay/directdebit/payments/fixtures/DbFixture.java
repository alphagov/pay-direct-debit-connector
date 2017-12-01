package uk.gov.pay.directdebit.payments.fixtures;

public interface DbFixture<F, E> {
    F insert();
    E toEntity();
}
