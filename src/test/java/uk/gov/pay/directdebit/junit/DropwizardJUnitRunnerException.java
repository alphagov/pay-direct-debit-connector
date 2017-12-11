package uk.gov.pay.directdebit.junit;

public final class DropwizardJUnitRunnerException extends RuntimeException {

    DropwizardJUnitRunnerException(String message) {
        super(message);
    }

    DropwizardJUnitRunnerException(Throwable cause) {
        super(cause);
    }
}
