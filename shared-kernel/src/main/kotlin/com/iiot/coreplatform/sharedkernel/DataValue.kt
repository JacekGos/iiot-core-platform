package com.iiot.coreplatform.sharedkernel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The value carried by a [DataEvent]. Only these five types are supported.
 *
 * Serialized with its type, e.g. `{"type":"INT","value":42}`, so a round trip keeps the exact type
 * (plain JSON can't tell Int `5` from Long `5`).
 */
@Serializable
sealed class DataValue {
    @Serializable
    @SerialName("INT")
    data class IntValue(
        val value: Int,
    ) : DataValue()

    @Serializable
    @SerialName("LONG")
    data class LongValue(
        val value: Long,
    ) : DataValue()

    @Serializable
    @SerialName("DOUBLE")
    data class DoubleValue(
        val value: Double,
    ) : DataValue()

    @Serializable
    @SerialName("BOOLEAN")
    data class BooleanValue(
        val value: Boolean,
    ) : DataValue()

    @Serializable
    @SerialName("STRING")
    data class StringValue(
        val value: String,
    ) : DataValue()

    companion object {
        /** Wraps a raw value; throws [IllegalArgumentException] for any type other than the five supported. */
        fun of(raw: Any): DataValue =
            when (raw) {
                is Int -> IntValue(raw)
                is Long -> LongValue(raw)
                is Double -> DoubleValue(raw)
                is Boolean -> BooleanValue(raw)
                is String -> StringValue(raw)
                else -> throw IllegalArgumentException("Unsupported DataValue type: ${raw::class.qualifiedName}")
            }
    }
}
