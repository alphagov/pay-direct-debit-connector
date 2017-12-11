package uk.gov.pay.directdebit.junit;

public class PostgresTestDockerException extends RuntimeException {

    public PostgresTestDockerException(String message) {
        super(message);
    }

    public PostgresTestDockerException(Throwable cause) {
        super(cause);
    }
}
