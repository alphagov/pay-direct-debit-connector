package uk.gov.pay.directdebit.common.validation;

public class FieldSize {

    private Integer minimum;

    private Integer maximum;

    public FieldSize(Integer minimum, Integer maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Integer getMinimum() {
        return minimum;
    }

    public Integer getMaximum() {
        return maximum;
    }

}
