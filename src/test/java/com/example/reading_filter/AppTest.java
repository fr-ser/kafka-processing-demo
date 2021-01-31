package com.example.reading_filter;

import com.example.reading_filter.AppSerdes.FilterConditionValue;
import com.example.reading_filter.AppSerdes.ReadingIdKey;
import com.example.reading_filter.AppSerdes.ReadingValue;
import org.apache.kafka.streams.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {
    Config config = new Config();

    TopologyTestDriver testDriver;
    TestInputTopic<ReadingIdKey, FilterConditionValue> filterConditionTopic;
    TestInputTopic<ReadingIdKey, ReadingValue> inputTopic;
    TestOutputTopic<ReadingIdKey, ReadingValue> outputTopic;

    @Before
    public void setupTopologyTestDriver() throws IOException {
        Properties props = Config.getKafkaStreamsConfig();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        testDriver = new TopologyTestDriver(App.createTopology(), props);

        filterConditionTopic = testDriver.createInputTopic(
                config.FILTER_CONDITION_TOPIC,
                ReadingIdKey.serde.serializer(), FilterConditionValue.serde.serializer()
        );
        inputTopic = testDriver.createInputTopic(
                config.SOURCE_TOPIC, ReadingIdKey.serde.serializer(), ReadingValue.serde.serializer()
        );
        outputTopic = testDriver.createOutputTopic(
                config.DESTINATION_TOPIC, ReadingIdKey.serde.deserializer(), ReadingValue.serde.deserializer()
        );
    }

    @After
    public void closeTestDriver() {
        testDriver.close();
    }


    @Test
    public void publishUnknownMessage() {
        ReadingIdKey readingIdKey = new ReadingIdKey(12L);
        ReadingValue readingValue = new ReadingValue(33.0, 123L);

        inputTopic.pipeInput(readingIdKey, readingValue);
        assertThat(outputTopic.readKeyValue())
                .usingRecursiveComparison()
                .isEqualTo(new KeyValue<>(readingIdKey, readingValue));
    }


    @Test
    public void publishKnownMessage() {
        ReadingIdKey readingIdKey = new ReadingIdKey(12L);
        filterConditionTopic.pipeInput(readingIdKey, new FilterConditionValue(false));

        ReadingValue readingValue = new ReadingValue(33.0, 123L);

        inputTopic.pipeInput(readingIdKey, readingValue);

        assertThat(outputTopic.readKeyValue())
                .usingRecursiveComparison()
                .isEqualTo(new KeyValue<>(readingIdKey, readingValue));
    }

    @Test
    public void filterOutKnownMessage() {
        ReadingIdKey readingIdKey = new ReadingIdKey(12L);
        filterConditionTopic.pipeInput(readingIdKey, new FilterConditionValue(true));

        ReadingValue readingValue = new ReadingValue(33.0, 123L);

        inputTopic.pipeInput(readingIdKey, readingValue);
        assertThat(outputTopic.isEmpty()).isTrue();
    }
}
