package za.co.topcode.locationtracker.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.HttpException
import za.co.topcode.locationtracker.Constants
import za.co.topcode.locationtracker.LocationTrackerApp
import za.co.topcode.locationtracker.R
import za.co.topcode.locationtracker.model.PositionDto
import za.co.topcode.locationtracker.network.contract.NetworkManager
import za.co.topcode.locationtracker.util.GeneralHelper
import za.co.topcode.locationtracker.util.ServerException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        val id: String = "myTestId"
        val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm:ss a")
    }

    private var trackLocation = false
    private lateinit var rxPermissions: RxPermissions
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var networkManager: NetworkManager
    private var compositeDisposable: CompositeDisposable? = null
        get() = if (field == null || field!!.isDisposed) CompositeDisposable() else field

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG, "onCreate")
        rxPermissions = RxPermissions(this)
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        networkManager = GeneralHelper.getNetworkManager(this)
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
        /*if(trackLocation) {
            Log.i(TAG, "Tracking location enabled")
            requestLocationUpdates()
        }*/
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)

        outState?.putString(Constants.EXTRA_ACCURACY_TEXT, tv_value_accuracy.text.toString())
        outState?.putString(Constants.EXTRA_BEARING_TEXT, tv_value_bearing.text.toString())
        outState?.putString(Constants.EXTRA_LATITUDE_TEXT, tv_value_latitude.text.toString())
        outState?.putString(Constants.EXTRA_LONGITUDE_TEXT, tv_value_longitude.text.toString())
        outState?.putString(Constants.EXTRA_SPEED_TEXT, tv_value_speed.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        tv_value_accuracy.text = savedInstanceState?.getString(Constants.EXTRA_ACCURACY_TEXT)
        tv_value_bearing.text = savedInstanceState?.getString(Constants.EXTRA_BEARING_TEXT)
        tv_value_latitude.text = savedInstanceState?.getString(Constants.EXTRA_LATITUDE_TEXT)
        tv_value_longitude.text = savedInstanceState?.getString(Constants.EXTRA_LONGITUDE_TEXT)
        tv_value_speed.text = savedInstanceState?.getString(Constants.EXTRA_SPEED_TEXT)
        tv_value_time.text = dateFormat.format(Date())
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        stopService((applicationContext as LocationTrackerApp).locationIntent)
        localBroadcastManager.unregisterReceiver(receiver)
        compositeDisposable?.dispose()
        super.onDestroy()
    }

    fun onStartTrackingClick(view: View) {
        trackLocation = true
        requestLocationUpdates()
    }

    fun onStopTrackingClick(view: View) {
        trackLocation = false
        stopService((applicationContext as LocationTrackerApp).locationIntent)
        localBroadcastManager.unregisterReceiver(receiver)
    }

    fun onClearDataClick(view: View) {
        tv_value_accuracy.text = "-"
        tv_value_bearing.text = "-"
        tv_value_latitude.text = "-"
        tv_value_longitude.text = "-"
        tv_value_speed.text = "-"
        tv_value_time.text = "-"
        sendClearLocations()
    }

    fun onOpenMapClick(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun requestLocationUpdates() {

        if (!rxPermissions.isGranted(Manifest.permission.ACCESS_FINE_LOCATION) || !rxPermissions.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            compositeDisposable?.add(rxPermissions.request(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
                .doOnDispose { Log.i(TAG, "Disposing requestLocationUpdates Single") }
                .subscribe { granted ->
                    if (granted) {
                        Log.i(TAG, "Permissions set")
                        stopService((applicationContext as LocationTrackerApp).locationIntent)
                        startService((applicationContext as LocationTrackerApp).locationIntent)
                        localBroadcastManager.registerReceiver(receiver, IntentFilter(Constants.INTENT_ACTION_LOCATION_UPDATE))
                    } else {
                        Log.i(TAG, "Permissions not set")
                    }
                })
        } else {
            Log.d(TAG, "Permissions Already set")
            stopService((applicationContext as LocationTrackerApp).locationIntent)
            startService((applicationContext as LocationTrackerApp).locationIntent)
            localBroadcastManager.registerReceiver(receiver, IntentFilter(Constants.INTENT_ACTION_LOCATION_UPDATE))
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.action?.let {action ->
                if (action.matches(Constants.INTENT_ACTION_LOCATION_UPDATE.toRegex())) {

                    Log.i(TAG, "onReceive INTENT_ACTION_LOCATION_UPDATE")
                    val bundle = intent.extras

                    val latitude: Double = bundle?.getDouble(Constants.EXTRA_LATITUDE)?: 0.0
                    val longitude: Double = bundle?.getDouble(Constants.EXTRA_LONGITUDE)?: 0.0
                    val accuracy: Float = bundle?.getFloat(Constants.EXTRA_ACCURACY)?: 0f
                    val bearing: Float = bundle?.getFloat(Constants.EXTRA_BEARING)?: 0f
                    val speed: Float = bundle?.getFloat(Constants.EXTRA_SPEED)?: 0f

                    tv_value_accuracy.text = "$accuracy"
                    tv_value_bearing.text = "$bearing"
                    tv_value_latitude.text = "$latitude"
                    tv_value_longitude.text = "$longitude"
                    tv_value_speed.text = "$speed"
                    tv_value_time.text = dateFormat.format(Date())

                    val positionDto = PositionDto(accuracy.toDouble(), bearing.toDouble(), latitude, longitude, speed.toDouble(), Date().time)

                    sendLocationUpdate(positionDto)
                }
            }


        }
    }

    private fun sendLocationUpdate(positionDto: PositionDto) {
        compositeDisposable?.add(networkManager.updateLocation(id, positionDto)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i(TAG, "sendLocationUpdate success")
            }, { e ->
                val error = processError(e)
                error.printStackTrace()
                Log.i(TAG, "sendLocationUpdate Error: ${error.message}")
            })
        )
    }

    private fun sendClearLocations() {
        compositeDisposable?.add(networkManager.clearLocations(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i(TAG, "sendClearLocations success")
            }, { e ->
                val error = processError(e)
                error.printStackTrace()
                Log.i(TAG, "sendClearLocations Error: ${error.message}")
            })
        )
    }

    private fun processError(error: Throwable): Throwable {
        Log.i(TAG, "Error: " + GeneralHelper.getJsonFromObject(error))
        return if(error is HttpException) {
            Log.i(TAG, "This is HttpException")
            ServerException(error.code(), error.response().errorBody()?.string(), error.cause)
        } else error
    }
}
