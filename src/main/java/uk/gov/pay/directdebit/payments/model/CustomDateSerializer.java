package uk.gov.pay.directdebit.payments.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.ZonedDateTime;

import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class CustomDateSerializer extends StdSerializer<ZonedDateTime> {

    CustomDateSerializer() {
        this(null);
    }

    private CustomDateSerializer(Class<ZonedDateTime> t) {
        super(t);
    }

    @Override
    public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider sp) throws IOException {
        gen.writeString(ISO_INSTANT_MILLISECOND_PRECISION.format(value));
    }
}
