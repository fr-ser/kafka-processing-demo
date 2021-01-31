package com.example.reading_filter

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.streams.KafkaStreams.{State, StateListener}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import java.time.Duration
import java.util.Properties

object AppStateListener extends StateListener with LazyLogging {
  def onChange(newState: State, oldState: State): Unit = {
    logger.info(s"Current state is: $newState (prev: $oldState)")
  }
}

object App extends App with LazyLogging {
  val appConfig = ConfigSource.default.loadOrThrow[AppConfig]

  val kStreamsConfig = new Properties()
  kStreamsConfig.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, appConfig.applicationName)
  kStreamsConfig.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, appConfig.bootstrapServers)
  kStreamsConfig.setProperty(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, appConfig.commitIntervalMs.toString)

  val topology = ReadingFilter.getTopology(appConfig)

  logger.info(topology.describe.toString)

  val streams = new KafkaStreams(topology, kStreamsConfig)
  streams.setStateListener(AppStateListener)
  streams.start()

  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }
}
