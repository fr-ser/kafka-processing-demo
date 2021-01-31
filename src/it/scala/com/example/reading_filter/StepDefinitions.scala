package com.example.reading_filter

import com.example.reading_filter.models.{JsonSerde, ReadingId, ReadingValue}
import io.cucumber.datatable.DataTable
import io.cucumber.scala.{EN, ScalaDsl}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serde
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.time.Duration
import java.util.Collections
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class StepDefinitions extends ScalaDsl with EN {
  import JsonSerde._

  private def readingIdDeserializer(input: String)(implicit serde: Serde[ReadingId]): ReadingId =
    serde.deserializer().deserialize("unused_topic", input.getBytes())
  private def readingValueDeserializer(input: String)(implicit serde: Serde[ReadingValue]): ReadingValue =
    serde.deserializer().deserialize("unused_topic", input.getBytes())

  private val consumer = KafkaHelper.getConsumer
  private val producer = KafkaHelper.getProducer

  After({
    producer.close()
    consumer.close()
  })

  Given("""^we clear the conditions for "([^"]*)" in "filter-condition"$""") { (reading_ids: String) =>
    for (reading_id <- reading_ids.split(",")) {
      producer.send(
        new ProducerRecord[String, String]("filter-condition", s"""{"reading_id":$reading_id}""", null)
      ).get()
    }
    producer.flush()
  }

  Given("""^we listen on the kafka topic "filtered-readings"$""") { () =>
    consumer.subscribe(Collections.singletonList("filtered-readings"))

    val deadline = 10.seconds.fromNow
    while ( deadline.hasTimeLeft() && consumer.assignment.isEmpty) {
      consumer.poll(Duration.ofMillis(100))
    }
    if (consumer.assignment().isEmpty) throw new AssertionError("Did not get an assignment within 10 seconds")
  }
  Given("""we publish the following messages to {string}""") { (topic: String, dataTable: DataTable) =>
    dataTable.asMaps().forEach(row => {
      producer.send(
        new ProducerRecord[String, String](topic, row.get("key"), row.get("value"))
      ).get()
    })
    producer.flush()
  }

  Given("""we wait {int} seconds for the config to propagate""") { (timeout: Int) =>
    Thread.sleep(timeout)
  }

  Then("""^we expect to find the following messages in "filtered-readings"$""") { (dataTable: DataTable) =>
    val rows = dataTable.asMaps().iterator().asScala.toList
    KafkaHelper.getMessages(consumer, rows.size) match {
      case Left(value) => throw new AssertionError(value)
      case Right(messages) => if (messages.size != rows.size) {
        throw new AssertionError(s"Wanted ${rows.size} messages but got ${messages.size}")
      } else {
            for ((row, received) <- rows zip messages) {
              readingIdDeserializer(row.get("key")) shouldBe readingIdDeserializer(received.key())
              readingValueDeserializer(row.get("value")) shouldBe readingValueDeserializer(received.value())
            }
      }
    }
    }
}
