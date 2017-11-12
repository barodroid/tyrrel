package cat.helm.tyrrel.thermostat.model

import com.squareup.moshi.Json

/**
 * Created by hussein on 12/11/2017.
 */

data class ThermostatReading(
        @Json(name = "query_id") val queryId: String,
        @Json(name = "values") val read: ThermostatRead?)

data class ThermostatQuery(
//{"device": "thermostat", "keys": ["curtemp", "setpoint", "isheating"], "command": "get", "query_id": 830628922}        val device: String = "powermeter",
        val device: String = "thermostat",
        @Json(name = "keys") val keys: Array<String> = arrayOf("curtemp, setpoint, isheating "),
        val command: String = "get",
        @Json(name = "query_id") val queryId: String
)

data class ThermostatRead(
        val curtemp: Double?,
        val setpoint: Double,
        val isheating: Double
)

enum class Command(val text: String) {
    GET("get"),
    READINGS("readings")
}