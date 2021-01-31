package com.example.reading_filter.AppSerdes;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilterConditionValueTest {
    @Test
    public void serializesAndDeserializes() {
        String input = "{\"is_sensitive\":true}";

        Deserializer<FilterConditionValue> deserializer = FilterConditionValue.serde.deserializer();
        Serializer<FilterConditionValue> serializer = FilterConditionValue.serde.serializer();

        FilterConditionValue filterConditionValue = deserializer.deserialize("", input.getBytes());
        assertEquals(true, filterConditionValue.isSensitive);

        assertEquals(input, new String(serializer.serialize("", filterConditionValue)));
    }

}
