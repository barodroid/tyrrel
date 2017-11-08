package cat.helm.tyrrel.power;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import cat.helm.tyrrel.R;

public class PowerMeterActivity extends AppCompatActivity {

    public static void start(Activity context) {
        Intent intent = new Intent(context, PowerMeterActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_metter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }
}
