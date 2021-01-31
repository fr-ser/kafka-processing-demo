package com.example.reading_filter;

import com.example.reading_filter.AppSerdes.FilterConditionValue;
import com.example.reading_filter.AppSerdes.JoinedReadingValue;
import com.example.reading_filter.AppSerdes.ReadingIdKey;
import com.example.reading_filter.AppSerdes.ReadingValue;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.log4j.Logger;

import java.io.IOException;

public class App {
    private final static Logger logger = Logger.getRootLogger();
    private final static Config config = new Config();

    private static JoinedReadingValue joinReading(ReadingValue input, FilterConditionValue filter) {
        boolean isSensitive;
        if (filter != null) {
            isSensitive = filter.isSensitive;
        } else {
            isSensitive = false;
        }

        return new JoinedReadingValue(input.value, input. timestamp, isSensitive);
    }

    public static Topology createTopology() throws IOException {
        StreamsBuilder builder = new StreamsBuilder();

        GlobalKTable<ReadingIdKey, FilterConditionValue> configTable =
                builder.globalTable(Config.FILTER_CONDITION_TOPIC,
                        Consumed.with(ReadingIdKey.serde, FilterConditionValue.serde)
                );

        KStream<ReadingIdKey, ReadingValue> inputStream = builder.stream(
                config.SOURCE_TOPIC, Consumed.with(ReadingIdKey.serde, ReadingValue.serde)
        ).leftJoin(configTable,
                (ReadingIdKey key, ReadingValue value) -> key,
                App::joinReading
        ).filter(
                (key, value) -> !value.isSensitive
        ).mapValues(value -> new ReadingValue(value.value, value.timestamp));

        inputStream.to(config.DESTINATION_TOPIC, Produced.with(ReadingIdKey.serde, ReadingValue.serde));

        return builder.build();
    }

    public static void main(String[] args) throws IOException {
        Topology topology = App.createTopology();
        logger.info(topology.describe());

        KafkaStreams streams = new KafkaStreams(topology, Config.getKafkaStreamsConfig());
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
