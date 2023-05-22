package com.example.speedocheck2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class SpeedTrackerActivity : AppCompatActivity() {
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var speedTextView: TextView? = null
    private var distanceTextView: TextView? = null
    private var averageSpeedTextView: TextView? = null
    private var maximumSpeedTextView: TextView? = null
    private var previousLocation: Location? = null
    private var totalDistance: Float = 0f
    private var averageSpeed: Double = 0.0
    private var maximumSpeed: Double = 0.0
    private var numSpeedUpdates: Int = 0
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speed_tracker)

        speedTextView = findViewById(R.id.speedTextView)
        distanceTextView = findViewById(R.id.distanceTextView)
        averageSpeedTextView = findViewById(R.id.averageSpeedTextView)
        maximumSpeedTextView = findViewById(R.id.maximumSpeedTextView)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val speed = location.speed * 3.6
                speedTextView?.text = String.format("%.2f", speed) + " km/h"

                val prevLocation = previousLocation

                if (prevLocation != null) {
                    val distance = location.distanceTo(prevLocation)
                    totalDistance += distance
                }

                previousLocation = location

                numSpeedUpdates++
                averageSpeed = ((averageSpeed * (numSpeedUpdates - 1)) + speed) / numSpeedUpdates

                if (speed > maximumSpeed) {
                    maximumSpeed = speed
                }

                val formattedDistance = String.format("%.2f", totalDistance / 1000)
                distanceTextView?.text = "$formattedDistance km"

                val formattedAverageSpeed = String.format("%.2f", averageSpeed)
                val formattedMaximumSpeed = String.format("%.2f", maximumSpeed)
                averageSpeedTextView?.text = "Average Speed: $formattedAverageSpeed km/h"
                maximumSpeedTextView?.text = "Maximum Speed: $formattedMaximumSpeed km/h"

                // Store data in Firebase Realtime Database
                val speedData = hashMapOf("speed" to speed)
                val distanceData = hashMapOf("distance" to totalDistance)
                val averageSpeedData = hashMapOf("average_speed" to averageSpeed)
                val maximumSpeedData = hashMapOf("maximum_speed" to maximumSpeed)

                database.child("speed").push().setValue(speedData)
                database.child("distance").push().setValue(distanceData)
                database.child("average_speed").push().setValue(averageSpeedData)
                database.child("maximum_speed").push().setValue(maximumSpeedData)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000, // Time interval in milliseconds (1 second)
                0f,    // Minimum distance change in meters (0 meters)
                locationListener!!
            )
        }
    }
}
