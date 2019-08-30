package uk.gov.pay.directdebit.common.exception;

import java.util.ArrayDeque;

public class ExceptionSummariser {

    public static String summarise(Throwable e) {
        var sb = new StringBuilder();
        var throwables = new ArrayDeque<Throwable>();
        throwables.add(e);
        while (!throwables.isEmpty()) {
            var throwable = throwables.removeFirst();
            sb.append(throwable.getClass().getSimpleName());
            if (throwable.getMessage() != null) {
                sb.append(": ").append(throwable.getMessage());
            }
            if (throwable.getCause() != null) {
                sb.append(". Caused by ");
                throwables.add(throwable.getCause());
            }
        }
        return sb.toString();
    }

}
