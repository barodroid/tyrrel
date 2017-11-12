package cat.helm.tyrrel.thermostat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import cat.helm.tyrrel.BaseMqttActivity
import cat.helm.tyrrel.R
import kotlinx.android.synthetic.main.activity_thermostat.*
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttMessage

class ThermostatActivity : BaseMqttActivity() {

    override val mqttActionListner: IMqttMessageListener?
        get() = IMqttMessageListener { topic: String, message: MqttMessage ->
            val payload = String(message.payload)

            // message Arrived!

            if (isThermostatReading(payload)) {
                Log.i(cat.helm.tyrrel.TAG, "Message: TH $topic : $payload")
//                val issue = po werMeterReadingAdapter.fromJson(payload)


                Handler(mainLooper).post({
//                    speedView.speedTo(issue!!.read!!.power.toFloat())
                })
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thermostat)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

    }
    private fun isThermostatReading(payload: String) =
            payload.startsWith("{\"device\": \"thermostat\",") and
                payload.contains(" \"command\": \"readings\"", true)


    companion object {

        fun start(context: Activity) {
            val intent = Intent(context, ThermostatActivity::class.java)
            context.startActivity(intent)
        }
    }
}
