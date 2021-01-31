package com.example.reading_filter.models

import org.apache.kafka.common.serialization.Serde
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets.UTF_8


class ModelsSpec extends AnyFreeSpec with Matchers {
  import JsonSerde._

  "ReadingIdKey" - {
    def serdeReadingIdDes(bytes: Array[Byte])(implicit serde: Serde[ReadingId]): ReadingId =
      serde.deserializer().deserialize("unused_topic", bytes)
    def serdeReadingIdSer(readingId: ReadingId)(implicit serde: Serde[ReadingId]): Array[Byte] =
      serde.serializer().serialize("unused_topic", readingId)

    "should implicitly convert to kafka Serde" in {
      val readingIdKey           = ReadingId(12)
      val serializedKey = """{"reading_id":12}""".getBytes(UTF_8)

      serdeReadingIdDes(serializedKey) shouldBe readingIdKey
      serdeReadingIdDes(null) shouldBe null

      serdeReadingIdSer(readingIdKey) shouldBe serializedKey
      serdeReadingIdSer(null) shouldBe null
    }
  }

  "ReadingValue" - {
    def serdeReadingValueDes(bytes: Array[Byte])(implicit serde: Serde[ReadingValue]): ReadingValue =
      serde.deserializer().deserialize("unused_topic", bytes)
    def serdeReadingValueSer(readingValue: ReadingValue)(implicit serde: Serde[ReadingValue]): Array[Byte] =
      serde.serializer().serialize("unused_topic", readingValue)

    "should implicitly convert to kafka Serde" in {
      val readingValue           = ReadingValue(12.0, 33L)
      val serializedValue = """{"value":12.0,"timestamp":33}""".getBytes(UTF_8)

      serdeReadingValueDes(serializedValue) shouldBe readingValue
      serdeReadingValueDes(null) shouldBe null

      serdeReadingValueSer(readingValue) shouldBe serializedValue
      serdeReadingValueSer(null) shouldBe null
    }
  }

  "FilterConditionValue" - {
    def serdeFilterConditionValueDes(bytes: Array[Byte])(implicit serde: Serde[FilterConditionValue]): FilterConditionValue =
      serde.deserializer().deserialize("unused_topic", bytes)
    def serdeFilterConditionValueSer(readingValue: FilterConditionValue)(implicit serde: Serde[FilterConditionValue]): Array[Byte] =
      serde.serializer().serialize("unused_topic", readingValue)

    "should implicitly convert to kafka Serde" in {
      val filterConditionValue           = FilterConditionValue(true)
      val serializedValue = """{"is_sensitive":true}""".getBytes(UTF_8)

      serdeFilterConditionValueDes(serializedValue) shouldBe filterConditionValue
      serdeFilterConditionValueDes(null) shouldBe null

      serdeFilterConditionValueSer(filterConditionValue) shouldBe serializedValue
      serdeFilterConditionValueSer(null) shouldBe null
    }
  }
}
