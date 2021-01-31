package com.example.reading_filter.AppSerdes;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReadingIdKeyTest {

    @Test
    public void serializesAndDeserializes() {
        String input = "{\"reading_id\":1}";

        Deserializer<ReadingIdKey> deserializer = ReadingIdKey.serde.deserializer();
        Serializer<ReadingIdKey> serializer = ReadingIdKey.serde.serializer();

        ReadingIdKey readingIdKey = deserializer.deserialize("", input.getBytes());
        // TODO: ASK: : do I need this "delta" 0?
        assertEquals(readingIdKey.id, 1, 0);
        assertEquals(input, new String(serializer.serialize("", readingIdKey)));
    }
}
