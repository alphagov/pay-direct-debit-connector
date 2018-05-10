package uk.gov.pay.directdebit.payers.model;

public class GoCardlessBankAccountLookup {
    
    private final String bankName;
    private final boolean isBacs;
    
    public GoCardlessBankAccountLookup(String bankName, boolean isBacs) {
        this.bankName = bankName;
        this.isBacs = isBacs;
    }

    public String getBankName() {
        return bankName;
    }

    public boolean isBacs() {
        return isBacs;
    }
    
}
