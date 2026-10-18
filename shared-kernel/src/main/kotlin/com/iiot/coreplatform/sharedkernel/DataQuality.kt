package com.iiot.coreplatform.sharedkernel

import com.iiot.coreplatform.sharedkernel.serialization.DataQualitySerializer
import kotlinx.serialization.Serializable

/**
 * Protocol-neutral quality of a reading
 * Connectors map their native status to it; protocol-specific detail goes in [DataEvent.metadata].
 * Serialized as its [code], e.g. `"GOOD"`.
 */
@Serializable(with = DataQualitySerializer::class)
sealed class DataQuality(
    val code: String,
) {
    data object Good : DataQuality("GOOD")

    data object Bad : DataQuality("BAD")

    data object Uncertain : DataQuality("UNCERTAIN")

    data object Unknown : DataQuality("UNKNOWN")

    companion object {
        fun fromCode(code: String): DataQuality? =
            when (code) {
                Good.code -> Good
                Bad.code -> Bad
                Uncertain.code -> Uncertain
                Unknown.code -> Unknown
                else -> null
            }
    }
}
