package com.iiot.coreplatform.sharedkernel

import com.iiot.coreplatform.sharedkernel.serialization.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * A single reading of a tag from an industrial data source.
 *
 * Flows from the Connector Service through Kafka into the pipeline engine and beyond.
 * The Connector Service keeps a hand-written copy of this model.
 */
@Serializable
data class DataEvent(
    val timestamp:
        @Serializable(with = InstantSerializer::class)
        Instant,
    val sourceId: SourceId,
    val tagName: TagName,
    val value: DataValue,
    val quality: DataQuality,
    val metadata: Map<String, String> = emptyMap(),
)
