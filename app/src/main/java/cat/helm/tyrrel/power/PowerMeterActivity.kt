package cat.helm.tyrrel.power

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import cat.helm.tyrrel.BaseMqttActivity
import cat.helm.tyrrel.R
import cat.helm.tyrrel.power.model.PowerMeterReading
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_except_schedule.*
import kotlinx.android.synthetic.main.content_power_metter.*
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*


class PowerMeterActivity : BaseMqttActivity() {
    //    private val handler = Handler(Looper.getMainLooper())
//    private val runnable = Runnable { handler.postDelayed( {publishMessage() }, 1000) }
    private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    val powerMeterReadingAdapter = moshi.adapter(PowerMeterReading::class.java)

    val list: ArrayList<PowerMeterReading> = ArrayList()

    private val powerQuery = "{\"device\": \"powermeter\", \"keys\": [\"power\", \"volt\"], \"command\": \"get\", \"query_id\": 200001}"


    override val mqttActionListner: IMqttMessageListener?
        get() = IMqttMessageListener { topic: String, message: MqttMessage ->
            val payload = String(message.payload)
//            val test = powerMeterReadingAdapter.fromJson(String(message.payload))

            // message Arrived!
            val isPowerReading = payload.startsWith("{\"device\": \"powermeter\",") and
                    payload.contains(" \"command\": \"readings\"", true)

            if (isPowerReading) {
                Log.i(TAG, "Message: $topic : $payload")
                val issue = powerMeterReadingAdapter.fromJson(String(message.payload))
                if (issue != null) {
                    list.add(issue)
                }

                Handler(mainLooper).post({
                    //TODO: Main thread job
                    speedView.speedTo(issue!!.read.power.toFloat())
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
        speedView.maxSpeed = 6000
        speedView.unit = "W"
        speedView.stop()
        button.setOnClickListener(View.OnClickListener { v ->
            Thread({
                while (true) {
                    publishMessage(powerQuery)
                    Thread.sleep(1000)
                }
            }).start()
            v.visibility = View.GONE
        })

    }
}
