package cat.helm.tyrrel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cat.helm.tyrrel.power.PowerMeterActivity
import cat.helm.tyrrel.thermostat.ThermostatActivity
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.content_main_menu.*

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        setSupportActionBar(toolbar)
        ivPowermeter.setOnClickListener {
            PowerMeterActivity.start(this)
        }

        ivThermostat.setOnClickListener {
            ThermostatActivity.start(this)
        }
    }
}


