package cat.helm.tyrrel.power.model

import com.squareup.moshi.Json

/**
 * Created by hussein on 12/11/2017.
 */

data class PowerMeterReading(
        @Json(name = "query_id") val queryId: String,
        val values: PowerMeterRead?)


data class PowerMeterQuery(
//        private val powerQueryJson = "{\"device\": \"powermeter\", \"keys\": [\"power\", \"volt\"], \"command\": \"get\", \"query_id\": 200001}"
        val device: String = "powermeter",
        @Json(name = "keys") val keys: Array<String> = arrayOf("power"),
        val command: String = "get",
        @Json(name = "query_id") val queryId: String
)

data class PowerMeterRead(
        val volt: Double?,
        val power: Double
)

enum class Command(val text: String) {
    GET("get"),
    READINGS("readings")
}