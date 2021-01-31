package com.example.reading_filter.AppSerdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;


// TODO: All this boilerplate just to join a value...
public class JoinedReadingValue {
    public static final Serde<JoinedReadingValue> serde = Serdes.serdeFrom(
        new JoinedReadingValue.ReadingValueSerializer(), new JoinedReadingValue.ReadingValueDeserializer()
    );

    public Double value;
    public Long timestamp;
    public Boolean isSensitive;

    // TODO: ASK: this is only used in tests. Should this be a test helper?
    public JoinedReadingValue() {}
    public JoinedReadingValue(Double value, Long timestamp, Boolean isSensitive) {
        this.value = value;
        this.timestamp=timestamp;
        this.isSensitive=isSensitive;
    }

    public static class ReadingValueDeserializer implements Deserializer<JoinedReadingValue> {
        private final ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        @Override
        public JoinedReadingValue deserialize(String s, byte[] bytes) {
            if (bytes == null) return null;

            JoinedReadingValue data;
            try {
                data = objectMapper.readValue(bytes, JoinedReadingValue.class);
            } catch (Exception e) {
                throw new SerializationException(e);
            }

            return data;
        }
    }

    public static class ReadingValueSerializer implements Serializer<JoinedReadingValue> {
        private final ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        @Override
        public byte[] serialize(String s, JoinedReadingValue joinedReadingValue) {
            if (joinedReadingValue == null)
                return null;

            try {
                return objectMapper.writeValueAsBytes(joinedReadingValue);
            } catch (Exception e) {
                throw new SerializationException("Error serializing JSON message", e);
            }
        }
    }
}
