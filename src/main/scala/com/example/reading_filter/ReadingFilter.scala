package com.example.reading_filter

import com.example.reading_filter.models.{FilterConditionValue, JsonSerde, ReadingId, ReadingValue}
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.KStream

object ReadingFilter extends JsonSerde  {

  case class JoinedValue(value: Double, timestamp: Long, isSensitive: Boolean)

  def getTopology(appConfig: AppConfig): Topology = {
    val builder: StreamsBuilder = new StreamsBuilder()
    val filterCondition: GlobalKTable[ReadingId, FilterConditionValue] = builder.globalTable(appConfig.filterConditionTopic)
    val sourceStream: KStream[ReadingId, ReadingValue] = builder.stream(appConfig.sourceTopic)


    sourceStream.leftJoin(filterCondition)(
      (key, _)=> key,
      (readingValue, filterValue) => {
        val isSensitive = if (filterValue != null) filterValue.isSensitive else false
        JoinedValue(readingValue.value, readingValue.timestamp, isSensitive)
      },
    ).flatMapValues(
      v => if (v.isSensitive) List() else List(ReadingValue(v.value, v.timestamp))
    ).to(appConfig.outputTopic)

    builder.build()
  }
}
