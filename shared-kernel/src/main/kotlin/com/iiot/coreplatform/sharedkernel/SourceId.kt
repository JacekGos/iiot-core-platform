package com.iiot.coreplatform.sharedkernel

import kotlinx.serialization.Serializable

/** Identifies the connector/device a [DataEvent] originates from. Serialized as a plain string. */
@Serializable
@JvmInline
value class SourceId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "SourceId must not be blank" }
    }
}
