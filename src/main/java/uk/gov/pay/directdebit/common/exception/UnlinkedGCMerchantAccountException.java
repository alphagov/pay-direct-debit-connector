package uk.gov.pay.directdebit.common.exception;

public class UnlinkedGCMerchantAccountException extends RuntimeException {
    public UnlinkedGCMerchantAccountException(String message){
        super(message);
    }
}
