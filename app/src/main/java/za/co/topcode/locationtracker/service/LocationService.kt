package za.co.topcode.locationtracker.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import za.co.topcode.locationtracker.Constants
import za.co.topcode.locationtracker.util.GeneralHelper
import android.app.PendingIntent
import za.co.topcode.locationtracker.activity.MainActivity
import za.co.topcode.locationtracker.R
import za.co.topcode.locationtracker.util.NotificationHandler


class LocationService : Service() {

    companion object {
        val TAG: String = LocationService::class.java.simpleName
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val gpsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action!!.matches(LocationManager.PROVIDERS_CHANGED_ACTION.toRegex())) {
                if (GeneralHelper.isLocationEnabled(this@LocationService)) {
                    unregisterReceiver(this)
                    createLocationRequest()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        val notification = NotificationHandler.getNotification(this, MainActivity::class.java, "Location Tracker", "Tracking your Location",
            "Tracking your location", false, PendingIntent.FLAG_UPDATE_CURRENT, R.drawable.circle_gold_512)
        startForeground(Constants.NOTIFICATION_ID, notification)
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.i(TAG, "onBind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        createLocationRequest()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        stopLocationUpdates()
        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    fun createLocationRequest() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 30000
            fastestInterval = 15000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(
            applicationContext
        )

        if(checkPermissionLocation() && GeneralHelper.isLocationEnabled(this)) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            registerReceiver(gpsReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        }
    }

    private fun stopLocationUpdates() {
        Log.i(TAG, "stopLocationUpdates")
        if(::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?: return
            Log.i(TAG, "onLocationResult Locations: ${locationResult.locations.size}")

            for(location in locationResult.locations) {
                val intent = Intent(Constants.INTENT_ACTION_LOCATION_UPDATE)
                intent.putExtra(Constants.EXTRA_LATITUDE, location.latitude)
                intent.putExtra(Constants.EXTRA_LONGITUDE, location.longitude)
                intent.putExtra(Constants.EXTRA_ACCURACY, location.accuracy)
                intent.putExtra(Constants.EXTRA_BEARING, location.bearing)
                intent.putExtra(Constants.EXTRA_SPEED, location.speed)
                LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(intent)
            }
        }
    }

    private fun checkPermissionLocation(): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        Log.i(TAG, "checkPermissionLocation Granted: $granted")
        return granted
    }

}