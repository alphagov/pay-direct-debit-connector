package uk.gov.pay.directdebit.payments.model;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZonedDateTime;

import static junit.framework.TestCase.assertEquals;


public class CustomDateSerializerTest {
    private CustomDateSerializer serializer;

    @Before
    public void before() {
        serializer = new CustomDateSerializer();
    }

    @Test
    public void shouldSerializeWithMillisecondPrecision() throws IOException {
        ZonedDateTime testValue = ZonedDateTime.parse("2019-01-29T11:34:53.849012345Z");
        Writer jsonWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        final SerializerProvider serializerProvider = new ObjectMapper().getSerializerProvider();
        serializer.serialize(testValue, jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        final String actual = jsonWriter.toString();
        assertEquals("\"2019-01-29T11:34:53.849Z\"", actual);
    }
}
