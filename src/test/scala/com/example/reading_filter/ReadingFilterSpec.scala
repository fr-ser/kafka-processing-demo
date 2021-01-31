package com.example.reading_filter

import com.example.reading_filter.models.{JsonSerde, ReadingId, _}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.{StreamsConfig, TopologyTestDriver}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import java.util.Properties


class ReadingFilterSpec extends AnyFreeSpec with Matchers {
  import JsonSerde._

  private val topologyConfig = {
    val props = new Properties()
    props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "testing")
    props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "testing:1234")
    props
  }

  private val appConfig =  ConfigSource.default.loadOrThrow[AppConfig]


  private def readingIdDeserializer()(implicit serde: Serde[ReadingId]) = serde.deserializer()
  private def readingIdSerializer()(implicit serde: Serde[ReadingId]) = serde.serializer()
  private def readingValueSerializer()(implicit serde: Serde[ReadingValue]) = serde.serializer()
  private def filterConditionSerializer()(implicit serde: Serde[FilterConditionValue]) = serde.serializer()
  private def readingValueDeserializer()(implicit serde: Serde[ReadingValue]) = serde.deserializer()


  "The ReadingFilter" - {
    "should pass messages through if no filter is provided" in {
      val driver = new TopologyTestDriver(ReadingFilter.getTopology(appConfig), topologyConfig)

      try {
        val sourceTopic = driver.createInputTopic(appConfig.sourceTopic, readingIdSerializer, readingValueSerializer)
        val outputTopic = driver.createOutputTopic(appConfig.outputTopic, readingIdDeserializer, readingValueDeserializer)

        val key = ReadingId(12)
        val value = ReadingValue(144.0, 123456789L)
        sourceTopic.pipeInput(key, value)
        val record = outputTopic.readRecord()

        record.key() shouldBe key
        record.value() shouldBe value

      } finally {
        driver.close()
      }
    }

    "should pass messages through when the provided filter is not sensitive" in {
      val driver = new TopologyTestDriver(ReadingFilter.getTopology(appConfig), topologyConfig)

      try {
        val sourceTopic = driver.createInputTopic(appConfig.sourceTopic, readingIdSerializer, readingValueSerializer)
        val filterTopic = driver.createInputTopic(appConfig.filterConditionTopic, readingIdSerializer, filterConditionSerializer)
        val outputTopic = driver.createOutputTopic(appConfig.outputTopic, readingIdDeserializer, readingValueDeserializer)

        val key = ReadingId(12)
        val value = ReadingValue(144.0, 123456789L)
        filterTopic.pipeInput(key, FilterConditionValue(false))
        sourceTopic.pipeInput(key, value)
        val record = outputTopic.readRecord()

        record.key() shouldBe key
        record.value() shouldBe value

      } finally {
        driver.close()
      }
    }

    "should filter messages when the provided filter is sensitive" in {
      val driver = new TopologyTestDriver(ReadingFilter.getTopology(appConfig), topologyConfig)

      try {
        val sourceTopic = driver.createInputTopic(appConfig.sourceTopic, readingIdSerializer, readingValueSerializer)
        val filterTopic = driver.createInputTopic(appConfig.filterConditionTopic, readingIdSerializer, filterConditionSerializer)
        val outputTopic = driver.createOutputTopic(appConfig.outputTopic, readingIdDeserializer, readingValueDeserializer)

        val key = ReadingId(12)
        val value = ReadingValue(144.0, 123456789L)
        filterTopic.pipeInput(key, FilterConditionValue(true))
        sourceTopic.pipeInput(key, value)

        outputTopic.isEmpty shouldBe true
      } finally {
        driver.close()
      }
    }
  }
}
