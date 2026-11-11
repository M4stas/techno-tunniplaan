package io.github.m4stas.technotunniplaan.schedule

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDate = decoder.decodeString().let { try { LocalDate.parse(it) } catch (e: DateTimeParseException) { throw SerializationException("Invalid date $it", e) } }
}
object LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalTime) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalTime = decoder.decodeString().let { try { LocalTime.parse(it) } catch (e: DateTimeParseException) { throw SerializationException("Invalid time $it", e) } }
}
