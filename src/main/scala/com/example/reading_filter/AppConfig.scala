package com.example.reading_filter

case class AppConfig(
  bootstrapServers: String,
  sourceTopic: String,
  filterConditionTopic: String,
  outputTopic: String,
  commitIntervalMs: Long,
) {
  val applicationName = "example.reading-filter"
}
