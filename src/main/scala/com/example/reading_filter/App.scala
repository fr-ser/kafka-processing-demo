package com.example.reading_filter

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

import java.time.Duration
import java.util.Properties

object App extends App with LazyLogging {
  val appConfig = AppConfig("localhost:9092", "readings", "filter-condition", "filtered-readings")

  val kStreamsConfig = new Properties()
  kStreamsConfig.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "example.reading-filter")
  kStreamsConfig.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, appConfig.bootstrapServers)

  val topology = ReadingFilter.getTopology(appConfig)

  logger.info(topology.describe.toString)

  val streams = new KafkaStreams(topology, kStreamsConfig)
  streams.start()

  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }
}
