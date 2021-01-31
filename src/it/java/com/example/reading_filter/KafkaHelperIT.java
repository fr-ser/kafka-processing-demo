package com.example.reading_filter;

import com.example.reading_filter.AppSerdes.FilterConditionValue;
import com.example.reading_filter.AppSerdes.ReadingIdKey;
import com.example.reading_filter.AppSerdes.ReadingValue;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class KafkaHelperIT {
    static KafkaProducer getFilterConditionProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ReadingIdKey.serde.serializer().getClass());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                FilterConditionValue.serde.serializer().getClass());

        return new KafkaProducer(props);
    }

    static KafkaProducer getReadingProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ReadingIdKey.serde.serializer().getClass());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ReadingValue.serde.serializer().getClass());

        return new KafkaProducer(props);
    }

    /**
     * creates a new consumer and waits for it to be assigned before it returns
     *
     * @return consumer assigned to the destination topic
     */
    static KafkaConsumer getOutputReadingsConsumer() {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ReadingIdKey.serde.deserializer().getClass());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ReadingValue.serde.deserializer().getClass());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "integration-test-consumer-"+Instant.now().getEpochSecond());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer consumer = new KafkaConsumer(props);
        consumer.subscribe(Arrays.asList(Config.DESTINATION_TOPIC));
        while (consumer.assignment().size() == 0) {
            consumer.poll(Duration.ofMillis(100));
        }

        return consumer;
    }


    /**
     * This method tries to get at least the expectedMessageCount of messages before it returns.
     * It waits for a timeout until it has enough messages or when enough messages have arrived it waits to receive
     * no new messages for a while before returning
     *
     * @param consumer             initialized consumer to read messages from
     * @param expectedMessageCount the number of messages for which to wait
     * @return the list of received messages
     */
    static List<ConsumerRecord> getMessages(KafkaConsumer consumer, Integer expectedMessageCount) throws TimeoutException {
        List<ConsumerRecord> records = new ArrayList<ConsumerRecord>();

        Instant endTime = Instant.now().plusSeconds(10);

        while (endTime.isAfter(Instant.now())) {
            ConsumerRecords<ReadingIdKey, ReadingValue> newRecords = consumer.poll(Duration.ofSeconds(1));

            if (newRecords.isEmpty()) {
                if (records.size() >= expectedMessageCount) {
                    return records;
                }
            } else {
                newRecords.forEach(record -> records.add(record));
            }
        }

        throw new TimeoutException("Received " + records.size() + " message of the wanted " + expectedMessageCount +
                " within 10 seconds");
    }
}
