package cat.helm.tyrrel;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cat.helm.tyrrel.utils.SslHelper;


public class ProveOfConceptActivity extends AppCompatActivity {

    private static final String TAG = "TyrrelMainActivity";
    private static final String SERVER_URI = "ssl://dijkstra.auge.cat:8883";
    private static final String SUBSCRIPTION_TOPIC = "smarthome";

    private Context context;
    private SslHelper sslHelper;
    private String clientId;
    private MqttAndroidClient mqttAndroidClient;
    private int messageNumber = 0;

    @BindView(R.id.textView) TextView textView;
    @BindView(R.id.buttonConnect) Button buttonConnect;


    // TODO: 02/11/2017 Implement publish logic
    final String publishTopic = "exampleAndroidPublishTopic";
    final String publishMessage = "Hello World!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    @OnClick(R.id.buttonConnect) void onButtonConnectClick() {
        subscribe();
    }

    private void init() {
        context = getApplicationContext();
        sslHelper = new SslHelper(context);
        clientId = "TyrrelClient: " + Build.BRAND + "::" + Build.MODEL + "::" + Build.ID;
    }

    private void subscribe() {
        clientId = clientId + System.currentTimeMillis();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), SERVER_URI, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    addToHistory("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    addToHistory("Connected to: " + serverURI);
                    buttonConnect.setText("Connected");
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                addToHistory("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setConnectionTimeout(60);
        mqttConnectOptions.setKeepAliveInterval(60);


        try {
            mqttConnectOptions.setSocketFactory(sslHelper.getSocketFactory());

            addToHistory("Connecting to " + SERVER_URI);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to connect to: " + SERVER_URI);
                    Log.e(TAG, "onFailure: ", exception);
                }
            });


        } catch (MqttException ex) {
            Log.e(TAG, "Subscribe: ", ex);
        }
    }

    private void addToHistory(String mainText) {
        Log.i(TAG, "LOG: " + mainText);

        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(SUBSCRIPTION_TOPIC, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    addToHistory("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to subscribe");
                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(SUBSCRIPTION_TOPIC, 0, (topic, message) -> {
                // message Arrived!
                Log.i(TAG, "Message: " + topic + " : " + new String(message.getPayload()));
                new Handler(getMainLooper()).post(() -> {
                    textView.setText(textView.getText() + "\n" +String.valueOf(messageNumber) + ": " + new String(message.getPayload()));
                    messageNumber++;
                });
            });

        } catch (MqttException ex) {
            Log.e(TAG, "Exception whilst subscribing", ex);
        }
    }

//I/TyrrelMainActivity: Message: smarthome : {"device": "powermeter", "keys": ["power", "volt"], "command": "get", "query_id": 232941662}

}
