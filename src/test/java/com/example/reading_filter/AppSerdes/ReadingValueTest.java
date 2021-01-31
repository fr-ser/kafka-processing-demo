package com.example.reading_filter.AppSerdes;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReadingValueTest {
    @Test
    public void serializesAndDeserializes() {
        String input = "{\"value\":23.0,\"timestamp\":12345}";

        Deserializer<ReadingValue> deserializer = ReadingValue.serde.deserializer();
        Serializer<ReadingValue> serializer = ReadingValue.serde.serializer();

        ReadingValue readingValue = deserializer.deserialize("", input.getBytes());
        assertEquals(23.0,readingValue.value, 0);
        assertEquals(12345,readingValue.timestamp, 0);

        assertEquals(input, new String(serializer.serialize("", readingValue)));
    }
}
