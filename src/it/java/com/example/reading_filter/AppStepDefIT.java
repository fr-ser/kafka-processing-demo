package com.example.reading_filter;

import com.example.reading_filter.AppSerdes.FilterConditionValue;
import com.example.reading_filter.AppSerdes.ReadingIdKey;
import com.example.reading_filter.AppSerdes.ReadingValue;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AppStepDefIT {
    KafkaProducer filterConditionProducer;
    KafkaProducer readingProducer;
    KafkaConsumer consumer;

    Deserializer<ReadingIdKey> readingIdDeserializer = ReadingIdKey.serde.deserializer();
    Deserializer<FilterConditionValue> filterConditionValueDeserializer = FilterConditionValue.serde.deserializer();
    Deserializer<ReadingValue> readingValueDeserializer = ReadingValue.serde.deserializer();

    @Before
    public void setup() {
        filterConditionProducer = KafkaHelperIT.getFilterConditionProducer();
        readingProducer = KafkaHelperIT.getReadingProducer();
    }

    @After
    public void teardown() {
        if (consumer != null) {
            consumer.close();
        }

        filterConditionProducer.close();
        readingProducer.close();
    }

    @Given("^we clear the conditions for \"([^\"]*)\" in \"filter-condition\"$")
    public void publish_tombstone_step(String reading_ids) throws ExecutionException, InterruptedException {
        for (String reading_id : reading_ids.split(",")) {
            filterConditionProducer.send(
                    new ProducerRecord(Config.FILTER_CONDITION_TOPIC, new ReadingIdKey(Long.parseLong(reading_id)),
                            null)
            ).get();
        }
        filterConditionProducer.flush();
    }

    @Given("^we listen on the kafka topic \"filtered-readings\"$")
    public void start_output_consumer_step() {
        consumer = KafkaHelperIT.getOutputReadingsConsumer();
    }

    @Given("^we publish the following messages to \"filter-condition\"$")
    public void publish_filter_condition_messages_step(List<List<String>> dataTable) throws ExecutionException, InterruptedException {
        List<String> headers = dataTable.remove(0);
        int keyIdx = headers.indexOf("key");
        int valueIdx = headers.indexOf("value");

        for (List<String> row : dataTable) {
            ReadingIdKey key = readingIdDeserializer.deserialize("", row.get(keyIdx).getBytes());
            FilterConditionValue value = filterConditionValueDeserializer.deserialize("", row.get(valueIdx).getBytes());

            filterConditionProducer.send(new ProducerRecord(Config.FILTER_CONDITION_TOPIC, key, value)).get();
        }
        filterConditionProducer.flush();
    }

    @Given("we wait {int} seconds for the config to propagate")
    public void we_wait_seconds_for_the_config_to_propagate(Integer sleepTime) throws InterruptedException {
        TimeUnit.SECONDS.sleep(sleepTime);
    }

    @Given("^we publish the following messages to \"readings\"$")
    public void publish_reading_messages_step(List<List<String>> dataTable) throws ExecutionException, InterruptedException {
        List<String> headers = dataTable.remove(0);
        int keyIdx = headers.indexOf("key");
        int valueIdx = headers.indexOf("value");

        for (List<String> row : dataTable) {
            ReadingIdKey key = readingIdDeserializer.deserialize("", row.get(keyIdx).getBytes());
            ReadingValue value = readingValueDeserializer.deserialize("", row.get(valueIdx).getBytes());

            readingProducer.send(new ProducerRecord(Config.SOURCE_TOPIC, key, value)).get();
        }
        readingProducer.flush();
    }

    @Then("^we expect to find the following messages in \"filtered-readings\"$")
    public void assert_destination_message_step(List<List<String>> dataTable) {
        List<ConsumerRecord> records = null;
        try {
            records = KafkaHelperIT.getMessages(consumer, dataTable.size() - 1);
        } catch (TimeoutException e) {
            fail(e.getMessage());
        }
        assertEquals(records.size(), dataTable.size() - 1);

        List<String> headers = dataTable.remove(0);
        int keyIdx = headers.indexOf("key");
        int valueIdx = headers.indexOf("value");

        for (int idx = 0; idx < records.size(); idx++) {
            ConsumerRecord<ReadingIdKey, ReadingValue> record = records.get(idx);
            List<String> row = dataTable.get(idx);

            ReadingIdKey key = readingIdDeserializer.deserialize("", row.get(keyIdx).getBytes());
            ReadingValue value = readingValueDeserializer.deserialize("", row.get(valueIdx).getBytes());

            assertThat(record.key()).usingRecursiveComparison().isEqualTo(key);
            assertThat(record.value()).usingRecursiveComparison().isEqualTo(value);
        }
    }
}