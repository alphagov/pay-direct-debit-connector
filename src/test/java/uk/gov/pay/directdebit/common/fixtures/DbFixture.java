package uk.gov.pay.directdebit.common.fixtures;

import org.jdbi.v3.core.Jdbi;

public interface DbFixture<F, E> {
    F insert(Jdbi jdbi);

    E toEntity();
}
