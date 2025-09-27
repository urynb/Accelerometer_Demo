package com.example.accelerometer

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null
    private var isFlat = false

    // onCreate runs when this activity starts
    // for the main activity, this is when you open the app.
    // setContentView marks which layout / UI you are using
    // layouts are stored in res/layout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // to use an element in your layout, you need to declare and assign a variable
        val proceedButton: Button = findViewById(R.id.proceedButton)

        // we access a sensor by creating a sensorManager object, and then
        // creating a sensor object of our desired type
        // because we want to check if the phone is flat on a surface, we use the Accelerometer
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // when you click the "Proceed" button, if the phone is flat on a surface, you move
        // to the data collection activity (CollectionActivity)
        // if not, the app creates a message notifying the user to put the phone flat against a surface
        proceedButton.setOnClickListener {
            if (isFlat) {
                startActivity(Intent(this, CollectionActivity::class.java))
            } else {
                Toast.makeText(this, "Place phone flat on surface", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // when the activity is relaunched, start listening for sensor input again
    override fun onResume() {
        super.onResume()
        accel?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    // when the activity is suspended, stop listening for sensor input
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // when the sensor readings from Accelerometer change, check if the phone is flat
    // if it is flat, update the variable isFlat
    // when isFlat is true, we can click on the proceed button
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            // Phone is flat if Z ≈ 9.8 and X,Y ≈ 0, but
            // the phone sensor sensitivity is limited so we use (2, 2, 8) instead
            isFlat = (Math.abs(x) < 2 && Math.abs(y) < 2 && z > 8)
        }
    }

    // we don't do anything when the accuracy changes
    // however, SensorEventListener needs BOTH onSensorChanged and onAccuracyChanged to be declared
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}