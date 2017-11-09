package cat.helm.tyrrel

import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import cat.helm.tyrrel.utils.SslHelper
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

abstract class BaseMqttActivity : AppCompatActivity() {
    private val SERVER_URI = "ssl://dijkstra.auge.cat:8883"
    private val SUBSCRIPTION_TOPIC = "smarthome"
    private val BASE_CLIENT_ID = "TyrrelClient: " + Build.BRAND + "::" + Build.MODEL + "::" + Build.ID
    internal val TAG = "MQTT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private lateinit var sslHelper: SslHelper
    private lateinit var clientId: String
    private var mqttAndroidClient: MqttAndroidClient? = null


    fun init() {
        clientId = BASE_CLIENT_ID + System.currentTimeMillis()
        sslHelper = SslHelper(applicationContext)
        subscribe()
    }

    private fun subscribe() {
        clientId += System.currentTimeMillis()

        mqttAndroidClient = MqttAndroidClient(applicationContext, SERVER_URI, clientId)
        mqttAndroidClient!!.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {

                if (reconnect) {
                    addToHistory("Reconnected to : " + serverURI)
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic()
                } else {
                    addToHistory("Connected to: " + serverURI)
//                    buttonConnect.setText("Connected")
                }
            }

            override fun connectionLost(cause: Throwable) {
                addToHistory("The Connection was lost.")
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                addToHistory("Incoming message: " + String(message.payload))
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {

            }
        })

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false
        mqttConnectOptions.connectionTimeout = 60
        mqttConnectOptions.keepAliveInterval = 60


        try {
            mqttConnectOptions.socketFactory = sslHelper.socketFactory

            addToHistory("Connecting to " + SERVER_URI)
            mqttAndroidClient!!.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttAndroidClient!!.setBufferOpts(disconnectedBufferOptions)
                    subscribeToTopic()
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    addToHistory("Failed to connect to: " + SERVER_URI)
                    Log.e(TAG, "onFailure: ", exception)
                }
            })


        } catch (ex: MqttException) {
            Log.e(TAG, "Subscribe: ", ex)
        }

    }

    abstract val mqttActionListner: IMqttMessageListener?

    fun subscribeToTopic() {
        try {
            mqttAndroidClient!!.subscribe(SUBSCRIPTION_TOPIC, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    addToHistory("Subscribed!")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    addToHistory("Failed to subscribe")
                }
            })

            mqttAndroidClient!!.subscribe(SUBSCRIPTION_TOPIC, 0, mqttActionListner)


        } catch (ex: MqttException) {
            Log.e(TAG, "Exception whilst subscribing", ex)
        }

    }

    private fun addToHistory(mainText: String) {
        Log.i(TAG, "LOG: " + mainText)

        Snackbar.make(findViewById<View>(android.R.id.content), mainText, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

    }

    override fun onStop() {
        super.onStop()
        mqttAndroidClient!!.disconnect()
    }
}
