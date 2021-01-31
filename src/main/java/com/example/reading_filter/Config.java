package com.example.reading_filter;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class Config {
    public static final String FILTER_CONDITION_TOPIC = "filter-condition";
    public static final String SOURCE_TOPIC = "readings";
    public static final String DESTINATION_TOPIC = "filtered-readings";

    public static Properties kafkaStreamsConfig;

    public static Properties getKafkaStreamsConfig() {
        if (kafkaStreamsConfig != null) {
            return kafkaStreamsConfig;
        }
        kafkaStreamsConfig = new Properties();

        String brokers = System.getenv().get("BOOTSTRAP_SERVERS");
        if (brokers == null) {
            throw new RuntimeException("The 'BOOTSTRAP_SERVERS' environment variable is not set");
        }

        kafkaStreamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream.reading-filter");
        kafkaStreamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        kafkaStreamsConfig.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once");
        kafkaStreamsConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Useful for debugging and to decrease latency in favor of network bandwidth
        if (System.getenv().get("COMMIT_INTERVAL_MS") != null) {
            kafkaStreamsConfig.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, Integer.parseInt(
                    System.getenv().get("COMMIT_INTERVAL_MS")
            ));
        }

        return kafkaStreamsConfig;
    }
}
