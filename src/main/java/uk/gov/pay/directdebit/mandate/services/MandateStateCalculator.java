package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import java.util.Optional;

public interface MandateStateCalculator {
    Optional<DirectDebitStateWithDetails<MandateState>> calculate(Mandate mandate);
}
