package cat.helm.tyrrel.power

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cat.helm.tyrrel.R
import kotlinx.android.synthetic.main.activity_except_schedule.*


class PowerMeterActivity : AppCompatActivity() {

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
