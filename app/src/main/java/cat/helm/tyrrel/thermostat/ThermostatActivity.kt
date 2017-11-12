package cat.helm.tyrrel.thermostat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import cat.helm.tyrrel.BaseMqttActivity
import cat.helm.tyrrel.R
import cat.helm.tyrrel.thermostat.model.ThermostatQuery
import cat.helm.tyrrel.thermostat.model.ThermostatReading
import kotlinx.android.synthetic.main.activity_thermostat.*
import kotlinx.android.synthetic.main.content_thermostat.*
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttMessage


class ThermostatActivity : BaseMqttActivity() {
    private val thermostatReadingAdapter = jsonTool.adapter(ThermostatReading::class.java)
    private val thermostatQueryAdapter = jsonTool.adapter(ThermostatQuery::class.java)
    private val thermostatQuery = ThermostatQuery(queryId = "200002")
    private val thermostatQueryJson = thermostatQueryAdapter.toJson(thermostatQuery)
    private var isRunning: Boolean = false

    private val pollingThread = Thread({

        while (isRunning) {
            publishMessage(thermostatQueryJson)
            Thread.sleep(1000)
        }

    })

    override val mqttActionListner: IMqttMessageListener?
        get() = IMqttMessageListener { topic: String, message: MqttMessage ->
            val payload = String(message.payload)

            // message Arrived!

            if (isThermostatReading(payload)) {
                Log.i(cat.helm.tyrrel.TAG, "Message: TH $topic : $payload")
                val read = thermostatReadingAdapter.fromJson(payload)


                Handler(mainLooper).post({
                    tvCurrentTempValue.text = String.format("%.2f ºC", read?.values?.curtemp)
                    tvSetPointValue.text = String.format("%.2f ºC", read?.values?.setpoint)
                    switch1.isChecked = read?.values?.isheating == 1
                })
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thermostat)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        object : CountDownTimer(30000, 1000) {


            override fun onTick(millisUntilFinished: Long) {
                nextUpdate.text = String.format("seconds remaining: %d", millisUntilFinished / 1000)
            }

            override fun onFinish() {
                try {
                    this.start()
                    publishMessage(thermostatQueryJson)

                } catch (ex: Exception) {

                }
            }
        }.start()

//        button.setOnClickListener({ view ->
//            isRunning = true
//            pollingThread.start()
//            view.visibility = View.GONE
//        })

    }

    private fun isThermostatReading(payload: String) =
            payload.startsWith("{\"device\": \"thermostat\",") and
                    payload.contains(" \"command\": \"readings\"", true)

    override fun onPause() {
        isRunning = false
        super.onPause()
    }

    companion object {

        fun start(context: Activity) {
            val intent = Intent(context, ThermostatActivity::class.java)
            context.startActivity(intent)
        }
    }
}
