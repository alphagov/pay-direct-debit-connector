package uk.gov.pay.directdebit.payments.api;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PaymentViewValidatorTest {

    private PaymentViewValidator validator = new PaymentViewValidator();

    @Test
    public void shouldReturnNoErrors() {
        List<Pair<String, Long>> pairList = ImmutableList.of(Pair.of("page", 2l), Pair.of("display_size", 50l));
        Optional<List> errors = validator.validateQueryParams(pairList);
        assertThat(errors.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAnError_whenPageisZero() {
        List<Pair<String, Long>> pairList = ImmutableList.of(Pair.of("page", 0l), Pair.of("display_size", 50l));
        Optional<List> errors = validator.validateQueryParams(pairList);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().get(0), is("query param 'page' should be a non zero positive integer"));
    }

    @Test
    public void shouldResetDisplaySizeTo500() {
        Pair<Long, Long> paginator = Pair.of(4l, 501l);
        Pair<Long, Long> result = PaymentViewValidator.validatePagination(Optional.of(paginator));
        assertThat(result.getRight(), is(500l));
    }
}
