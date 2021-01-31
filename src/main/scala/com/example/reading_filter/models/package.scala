package com.example.reading_filter

import io.circe.derivation.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, derivation}

// TODO: Ask: Is this the right place for this?
package object models {
    case class ReadingId(readingId:Int)
    implicit val decoderReadingId: Decoder[ReadingId] = deriveDecoder(derivation.renaming.snakeCase)
    implicit val encoderReadingId: Encoder[ReadingId] = deriveEncoder(derivation.renaming.snakeCase)

    case class ReadingValue(value: Double, timestamp: Long)
    implicit val decoderReadingValue: Decoder[ReadingValue] = deriveDecoder(derivation.renaming.snakeCase)
    implicit val encoderReadingValue: Encoder[ReadingValue] = deriveEncoder(derivation.renaming.snakeCase)

    case class FilterConditionValue(isSensitive: Boolean)
    implicit val decoderFilterConditionValue: Decoder[FilterConditionValue] = deriveDecoder(derivation.renaming.snakeCase)
    implicit val encoderFilterConditionValue: Encoder[FilterConditionValue] = deriveEncoder(derivation.renaming.snakeCase)

}
