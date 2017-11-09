package cat.helm.tyrrel.power

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import cat.helm.tyrrel.BaseMqttActivity
import cat.helm.tyrrel.R
import kotlinx.android.synthetic.main.activity_except_schedule.*
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttMessage


class PowerMeterActivity : BaseMqttActivity() {
    override val mqttActionListner: IMqttMessageListener?
        get() = IMqttMessageListener { topic: String, message: MqttMessage ->
            // message Arrived!
            if ( String(message.payload).startsWith("{\"device\": \"powermeter\",")) {
                Log.i(TAG, "Message: " + topic + " : " + String(message.getPayload()))
                Handler(mainLooper).post({
                    //TODO: Main thread job
                })
            }


        }


    companion object {

        fun start(context: Activity) {
            val intent = Intent(context, PowerMeterActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_power_metter)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

    }

}
