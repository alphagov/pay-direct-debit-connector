package uk.gov.pay.directdebit.common.fixtures;

import org.skife.jdbi.v2.DBI;

public interface DbFixture<F, E> {
    F insert(DBI jdbi);
    E toEntity();
}
