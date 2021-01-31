package com.example.reading_filter.AppSerdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;


public class ReadingValue {
    public static final Serde<ReadingValue> serde = Serdes.serdeFrom(
        new ReadingValue.ReadingValueSerializer(), new ReadingValue.ReadingValueDeserializer()
    );

    public Double value;
    public Long timestamp;

    // TODO: ASK: this is only used in tests. Should this be a test helper?
    public ReadingValue() {}
    public ReadingValue(Double value, Long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public static class ReadingValueDeserializer implements Deserializer<ReadingValue> {
        private final ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        @Override
        public ReadingValue deserialize(String s, byte[] bytes) {
            if (bytes == null) return null;

            ReadingValue data;
            try {
                data = objectMapper.readValue(bytes, ReadingValue.class);
            } catch (Exception e) {
                throw new SerializationException(e);
            }

            return data;
        }
    }

    public static class ReadingValueSerializer implements Serializer<ReadingValue> {
        private final ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        @Override
        public byte[] serialize(String s, ReadingValue readingValue) {
            if (readingValue == null)
                return null;

            try {
                return objectMapper.writeValueAsBytes(readingValue);
            } catch (Exception e) {
                throw new SerializationException("Error serializing JSON message", e);
            }
        }
    }
}
