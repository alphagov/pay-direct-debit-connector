package uk.gov.pay.directdebit.common.exception;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExceptionSummariserTest {

    @Test
    public void exceptionWithNoMessage() {
        Exception exception = new RuntimeException();

        String summary = ExceptionSummariser.summarise(exception);

        assertThat(summary, is("RuntimeException"));
    }

    @Test
    public void exceptionWithMessage() {
        Exception exception = new RuntimeException("Something went wrong");
        
        String summary = ExceptionSummariser.summarise(exception);
        
        assertThat(summary, is("RuntimeException: Something went wrong"));
    }

    @Test
    public void exceptionWithNoMessageWrappingCauseWithNoMessage() {
        Exception exception = new RuntimeException(new IllegalArgumentException());

        String summary = ExceptionSummariser.summarise(exception);

        assertThat(summary, is("RuntimeException: java.lang.IllegalArgumentException. Caused by IllegalArgumentException"));
    }

    @Test
    public void exceptionWithMessageWrappingCauseWithNoMessage() {
        Exception exception = new RuntimeException("Something went wrong", new IllegalArgumentException());

        String summary = ExceptionSummariser.summarise(exception);

        assertThat(summary, is("RuntimeException: Something went wrong. Caused by IllegalArgumentException"));
    }

    @Test
    public void exceptionWithNoMessageWrappingCauseWithMessage() {
        Exception exception = new RuntimeException(new IllegalArgumentException("Something was illegal"));

        String summary = ExceptionSummariser.summarise(exception);

        assertThat(summary, is("RuntimeException: java.lang.IllegalArgumentException: Something was illegal. " +
                "Caused by IllegalArgumentException: Something was illegal"));
    }

    @Test
    public void exceptionWithMessageWrappingCauseWithMessage() {
        Exception exception = new RuntimeException("Something went wrong", new IllegalArgumentException("Something was illegal"));

        String summary = ExceptionSummariser.summarise(exception);

        assertThat(summary, is("RuntimeException: Something went wrong. Caused by IllegalArgumentException: Something was illegal"));
    }

    @Test
    public void manyNestedExceptions() {
        Exception exception = new RuntimeException("Something went wrong",
                new IllegalArgumentException("Something was illegal", new NullPointerException("Something was illegal")));

        String summary = ExceptionSummariser.summarise(exception);

        assertThat(summary, is("RuntimeException: Something went wrong. Caused by IllegalArgumentException: Something was illegal. " +
                "Caused by NullPointerException: Something was illegal"));
    }

}
