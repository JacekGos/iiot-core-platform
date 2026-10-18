package com.iiot.coreplatform.sharedkernel.serialization

import com.iiot.coreplatform.sharedkernel.DataQuality
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Serializes [DataQuality] as its plain string code, e.g. `"GOOD"`. */
object DataQualitySerializer : KSerializer<DataQuality> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DataQuality", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: DataQuality,
    ) = encoder.encodeString(value.code)

    override fun deserialize(decoder: Decoder): DataQuality {
        val code = decoder.decodeString()
        return DataQuality.fromCode(code) ?: throw SerializationException("Unknown DataQuality code: '$code'")
    }
}
