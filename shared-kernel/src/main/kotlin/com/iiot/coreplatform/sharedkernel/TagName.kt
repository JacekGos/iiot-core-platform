package com.iiot.coreplatform.sharedkernel

import kotlinx.serialization.Serializable

/** Name of the tag (OPC-UA node, MQTT topic, ...) a [DataEvent] was read from. Serialized as a plain string. */
@Serializable
@JvmInline
value class TagName(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "TagName must not be blank" }
    }
}
