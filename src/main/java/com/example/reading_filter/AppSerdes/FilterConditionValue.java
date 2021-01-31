package com.example.reading_filter.AppSerdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;


public class FilterConditionValue {
    public static final Serde<FilterConditionValue> serde = Serdes.serdeFrom(
            new FilterConditionValue.FilterConditionValueSerializer(),
            new FilterConditionValue.FilterConditionValueDeserializer()
    );

    public Boolean isSensitive;


    public FilterConditionValue() {}
    // TODO: ASK: this is only used in tests. Should this be a test helper?
    public FilterConditionValue(Boolean isSensitive) {
        this.isSensitive = isSensitive;
    }

    public static class FilterConditionValueDeserializer implements Deserializer<FilterConditionValue> {
        private final ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        @Override
        public FilterConditionValue deserialize(String s, byte[] bytes) {
            if (bytes == null) return null;

            FilterConditionValue data;
            try {
                data = objectMapper.readValue(bytes, FilterConditionValue.class);
            } catch (Exception e) {
                throw new SerializationException(e);
            }

            return data;
        }
    }

    public static class FilterConditionValueSerializer implements Serializer<FilterConditionValue> {
        private final ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        @Override
        public byte[] serialize(String s, FilterConditionValue readingValue) {
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
