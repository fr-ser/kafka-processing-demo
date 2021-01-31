package com.example.reading_filter.AppSerdes;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

public class ReadingIdKey {
    public static final Serde<ReadingIdKey> serde = Serdes.serdeFrom(
            new ReadingIdKey.ReadingIdKeySerializer(), new ReadingIdKey.ReadingIdKeyDeserializer()
    );

    // TODO: ASK: : Are getters and setters recommended here and if so, why?
    @JsonProperty("reading_id")
    public Long id;


    // TODO: ASK: this is only used in tests. Should this be a test helper?
    public ReadingIdKey() {}
    public ReadingIdKey(Long id) {
        this.id = id;
    }

    public static class ReadingIdKeyDeserializer implements Deserializer<ReadingIdKey> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public ReadingIdKey deserialize(String s, byte[] bytes) {
            if (bytes == null) return null;

            ReadingIdKey data;
            try {
                data = objectMapper.readValue(bytes, ReadingIdKey.class);
            } catch (Exception e) {
                throw new SerializationException(e);
            }

            return data;
        }
    }

    public static class ReadingIdKeySerializer implements Serializer<ReadingIdKey> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public byte[] serialize(String s, ReadingIdKey readingIdKey) {
            if (readingIdKey == null)
                return null;

            try {
                return objectMapper.writeValueAsBytes(readingIdKey);
            } catch (Exception e) {
                throw new SerializationException("Error serializing JSON message", e);
            }
        }
    }
}
