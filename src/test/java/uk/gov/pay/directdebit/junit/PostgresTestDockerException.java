package uk.gov.pay.directdebit.junit;

class PostgresTestDockerException extends RuntimeException {

    PostgresTestDockerException(String message) {
        super(message);
    }

    PostgresTestDockerException(Throwable cause) {
        super(cause);
    }
}
