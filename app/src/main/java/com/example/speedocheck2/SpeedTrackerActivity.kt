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

class SpeedTrackerActivity : AppCompatActivity() {
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var speedTextView: TextView? = null
    private var distanceTextView: TextView? = null
    private var averageSpeedTextView: TextView? = null // Declare averageSpeedTextView
    private var maximumSpeedTextView: TextView? = null // Declare maximumSpeedTextView
    private var previousLocation: Location? = null
    private var totalDistance: Float = 0f
    private var averageSpeed: Double = 0.0
    private var maximumSpeed: Double = 0.0
    private var numSpeedUpdates: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speed_tracker)

        speedTextView = findViewById(R.id.speedTextView)
        distanceTextView = findViewById(R.id.distanceTextView)
        averageSpeedTextView = findViewById(R.id.averageSpeedTextView) // Initialize averageSpeedTextView
        maximumSpeedTextView = findViewById(R.id.maximumSpeedTextView) // Assuming you have a TextView with the ID distanceTextView

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val speed = location.speed * 3.6
                speedTextView?.text = String.format("%.2f", speed) + " km/h"

                val prevLocation = previousLocation // Store the value of previousLocation in a local variable

                // Calculate distance
                if (prevLocation != null) {
                    val distance = location.distanceTo(prevLocation)
                    totalDistance += distance
                }

                // Update previousLocation
                previousLocation = location

                // Calculate average speed
                numSpeedUpdates++
                averageSpeed = ((averageSpeed * (numSpeedUpdates - 1)) + speed) / numSpeedUpdates

                // Calculate maximum speed
                if (speed > maximumSpeed) {
                    maximumSpeed = speed
                }

                // Display the distance traveled
                val formattedDistance = String.format("%.2f", totalDistance / 1000) // Convert to kilometers
                distanceTextView?.text = "$formattedDistance km"

                // Display the average and maximum speed
                val formattedAverageSpeed = String.format("%.2f", averageSpeed)
                val formattedMaximumSpeed = String.format("%.2f", maximumSpeed)
                averageSpeedTextView?.text = "Average Speed: $formattedAverageSpeed km/h"
                maximumSpeedTextView?.text = "Maximum Speed: $formattedMaximumSpeed km/h"
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
