package cat.helm.tyrrel.power

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import cat.helm.tyrrel.BaseMqttActivity
import cat.helm.tyrrel.R
import cat.helm.tyrrel.power.model.PowerMeterQuery
import cat.helm.tyrrel.power.model.PowerMeterReading
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_except_schedule.*
import kotlinx.android.synthetic.main.content_power_metter.*
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttMessage


class PowerMeterActivity : BaseMqttActivity() {
    private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    private val powerMeterReadingAdapter = moshi.adapter(PowerMeterReading::class.java)
    private val powerMeterQueryAdapter = moshi.adapter(PowerMeterQuery::class.java)
    private val powerMeterQuery = PowerMeterQuery(queryId = "200001")
    private val powerQueryJson = powerMeterQueryAdapter.toJson(powerMeterQuery)

    private var isRunning: Boolean = false
    private val pollingThread = Thread({

        while (isRunning) {
            publishMessage(powerQueryJson)
            Thread.sleep(1000)
        }

    })

    override val mqttActionListner: IMqttMessageListener?
        get() = IMqttMessageListener { topic: String, message: MqttMessage ->
            val payload = String(message.payload)

            // message Arrived!

            if (isPowerReading(payload)) {
                Log.i(TAG, "Message: $topic : $payload")
                val issue = powerMeterReadingAdapter.fromJson(payload)


                Handler(mainLooper).post({
                    speedView.speedTo(issue!!.read!!.power.toFloat())
                })
            }

        }

    private fun isPowerReading(payload: String) =
            payload.startsWith("{\"device\": \"powermeter\",") and
                    payload.contains(" \"command\": \"readings\"", true)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_power_metter)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        speedView.maxSpeed = 6000
        speedView.unit = "W"
        button.setOnClickListener({ view ->
            isRunning = true
            pollingThread.start()
            view.visibility = View.GONE
        })
    }

    override fun onPause() {
        isRunning = false
        super.onPause()
    }

    companion object {

        fun start(context: Activity) {
            val intent = Intent(context, PowerMeterActivity::class.java)
            context.startActivity(intent)
        }
    }
}
