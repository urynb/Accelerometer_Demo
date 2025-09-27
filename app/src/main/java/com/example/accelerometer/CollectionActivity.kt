package com.example.accelerometer

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.hardware.*
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class CollectionActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null
    private var gyro: Sensor? = null

    private lateinit var rawButton: Button
    private lateinit var sineButton: Button
    private lateinit var outputText: TextView

    private val accelData = mutableListOf<String>()
    private val gyroData = mutableListOf<String>()
    private var collecting = false
    private var count = 0
    private var playSine = false
    private var audioTrack: AudioTrack? = null

    // onCreate runs when this activity starts
    // for the collection activity, this is when you click the "Proceed" button.
    // setContentView marks which layout / UI you are using
    // layouts are stored in res/layout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)

        // this marks out our three UI elements
        rawButton = findViewById(R.id.rawButton)
        sineButton = findViewById(R.id.sineButton)
        outputText = findViewById(R.id.outputText)

        // we access a sensor by creating a sensorManager object, and then
        // creating a sensor object of our desired type
        // here, we define both the Accelerometer and Gyroscope
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // the raw Button collects raw Accelerometer and Gyroscope data,
        // so it starts data collection without emitting a sine wave
        rawButton.setOnClickListener {
            startCollection(false)
        }

        // the sine Button collects Accelerometer and Gyroscope data
        // while emitting a sine wave
        sineButton.setOnClickListener {
            startCollection(true)
        }
    }

    // this function starts collecting data from the sensors
    // when it is called, it clears the existing data collected by the sensor
    // it is called by both the raw data button and the sine button, but only by 1 at a time
    private fun startCollection(withSine: Boolean) {
        rawButton.isEnabled = false
        sineButton.isEnabled = false
        accelData.clear()
        gyroData.clear()
        count = 0
        collecting = true
        playSine = withSine

        accel?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyro?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // if it is called with the sine button, also plays a 20k Hz wave
        if (withSine) playSineWave(20000.0)
    }

    // stops collecting data
    // if a sine wave is being emitted, also stops that
    // finally, it displays the collected data within our TextView, so we can see it on the screen
    private fun stopCollection() {
        sensorManager.unregisterListener(this)
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        collecting = false

        // store the data as a string
        val sb = StringBuilder()
        sb.append("Accelerometer:\n")
        accelData.forEach { sb.append(it).append("\n") }
        sb.append("\nGyroscope:\n")
        gyroData.forEach { sb.append(it).append("\n") }

        // display it
        outputText.text = sb.toString()
        rawButton.isEnabled = true
        sineButton.isEnabled = true
    }

    // when the sensors change, update data and increment Count
    // if Count > 5 (we've collected data 5 times) stop collection
    override fun onSensorChanged(event: SensorEvent?) {
        if (!collecting || event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> accelData.add("(${event.values[0]}, ${event.values[1]}, ${event.values[2]})")
            Sensor.TYPE_GYROSCOPE -> gyroData.add("(${event.values[0]}, ${event.values[1]}, ${event.values[2]})")
        }

        if (++count >= 10) stopCollection()
    }

    // we need to define a function onAccuracyChanged for the SensorEventListener, but
    // we don't actually use it here
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // plays the sine wave.
    // not very relevant for this course.
    private fun playSineWave(frequency: Double) {
        val sampleRate = 44100
        val count = (sampleRate * 1).toInt() // 1 second
        val buffer = ShortArray(count)

        for (i in buffer.indices) {
            val angle = 2.0 * Math.PI * i.toDouble() * frequency / sampleRate
            buffer[i] = (Math.sin(angle) * Short.MAX_VALUE).toInt().toShort()
        }

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            buffer.size * 2,
            AudioTrack.MODE_STATIC
        )
        audioTrack?.write(buffer, 0, buffer.size)
        audioTrack?.play()
    }
}
