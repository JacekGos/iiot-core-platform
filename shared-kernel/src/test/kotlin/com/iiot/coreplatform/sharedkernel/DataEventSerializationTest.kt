package com.iiot.coreplatform.sharedkernel

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DataEventSerializationTest {
    private val timestamp = Instant.parse("2026-10-18T11:34:00.123456789Z")

    private fun event(
        value: DataValue,
        quality: DataQuality = DataQuality.Good,
        metadata: Map<String, String> = emptyMap(),
    ) = DataEvent(
        timestamp = timestamp,
        sourceId = SourceId("plc-1"),
        tagName = TagName("temperature"),
        value = value,
        quality = quality,
        metadata = metadata,
    )

    private fun roundTrip(event: DataEvent): DataEvent = Json.decodeFromString(Json.encodeToString(event))

    @Test
    fun `round trip preserves every value type`() {
        listOf(
            DataValue.IntValue(42),
            DataValue.LongValue(42L),
            DataValue.DoubleValue(21.5),
            DataValue.DoubleValue(1.0),
            DataValue.BooleanValue(true),
            DataValue.StringValue("running"),
            DataValue.StringValue("42"),
        ).forEach { value ->
            val original = event(value)
            assertEquals(original, roundTrip(original))
        }
    }

    @Test
    fun `round trip preserves every quality`() {
        listOf(DataQuality.Good, DataQuality.Bad, DataQuality.Uncertain, DataQuality.Unknown).forEach { quality ->
            val original = event(DataValue.IntValue(1), quality = quality)
            assertEquals(original, roundTrip(original))
        }
    }

    @Test
    fun `round trip preserves metadata`() {
        val original = event(DataValue.IntValue(1), metadata = mapOf("unit" to "C", "nodeId" to "ns=2;s=Temp"))
        assertEquals(original, roundTrip(original))
    }

    @Test
    fun `serializes to the agreed JSON contract`() {
        val json = Json.encodeToString(event(DataValue.DoubleValue(21.5), metadata = mapOf("unit" to "C")))
        assertEquals(
            """{"timestamp":"2026-10-18T11:34:00.123456789Z","sourceId":"plc-1","tagName":"temperature",""" +
                """"value":{"type":"DOUBLE","value":21.5},"quality":"GOOD","metadata":{"unit":"C"}}""",
            json,
        )
    }

    @Test
    fun `DataValue of wraps each supported raw type`() {
        assertEquals(DataValue.IntValue(42), DataValue.of(42))
        assertEquals(DataValue.LongValue(42L), DataValue.of(42L))
        assertEquals(DataValue.DoubleValue(21.5), DataValue.of(21.5))
        assertEquals(DataValue.BooleanValue(true), DataValue.of(true))
        assertEquals(DataValue.StringValue("running"), DataValue.of("running"))
    }

    @Test
    fun `DataValue of rejects unsupported raw type`() {
        assertFailsWith<IllegalArgumentException> { DataValue.of(1.5f) }
    }

    @Test
    fun `rejects unknown quality code`() {
        val json = Json.encodeToString(event(DataValue.IntValue(1))).replace("\"GOOD\"", "\"EXCELLENT\"")
        assertFailsWith<SerializationException> { Json.decodeFromString<DataEvent>(json) }
    }

    @Test
    fun `rejects value not matching its type tag`() {
        val json = Json.encodeToString(event(DataValue.IntValue(1))).replace("\"type\":\"INT\"", "\"type\":\"BOOLEAN\"")
        assertFailsWith<SerializationException> { Json.decodeFromString<DataEvent>(json) }
    }
}
