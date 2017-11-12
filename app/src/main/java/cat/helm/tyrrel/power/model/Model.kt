package cat.helm.tyrrel.power.model

import com.squareup.moshi.Json

/**
 * Created by hussein on 12/11/2017.
 */
data class Query(
        val device: String,
        @Json(name = "query_id") val queryId: String,
        val command: String)

data class PowerMeterReading(
        val device: String,
        @Json(name = "query_id") val queryId: String,
        val command: String,
        @Json(name = "values") val read: PowerMeterRead)

data class PowerMeterRead(
        val volt: Double,
        val power: Double
)

enum class Command(val text: String) {
    GET("get"),
    READINGS("readings")
}