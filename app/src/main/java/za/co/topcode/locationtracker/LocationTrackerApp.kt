package za.co.topcode.locationtracker

import android.content.Context
import android.content.Intent
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import za.co.topcode.locationtracker.service.LocationService

class LocationTrackerApp : MultiDexApplication(){

    lateinit var locationIntent: Intent

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        locationIntent = Intent(this, LocationService::class.java)
    }
}