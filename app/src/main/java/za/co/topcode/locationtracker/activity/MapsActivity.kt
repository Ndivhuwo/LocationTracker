package za.co.topcode.locationtracker.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import com.here.oksse.OkSse
import android.view.View
import com.google.gson.reflect.TypeToken
import com.here.oksse.ServerSentEvent
import okhttp3.Request
import okhttp3.Response
import za.co.topcode.locationtracker.R
import za.co.topcode.locationtracker.model.PositionDto
import za.co.topcode.locationtracker.network.contract.NetworkManager
import za.co.topcode.locationtracker.util.GeneralHelper
import za.co.topcode.locationtracker.util.ServerException
import java.util.*
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        val TAG: String = MapsActivity::class.java.simpleName
        val id: String = "myTestId"
    }

    private lateinit var googleMap1: GoogleMap
    private lateinit var networkManager: NetworkManager
    private var compositeDisposable: CompositeDisposable? = null
        get() = if (field == null || field!!.isDisposed) CompositeDisposable() else field

    private var currentLocationMarker: Marker? = null
    private var locationAccuracyCircle: Circle? = null
    private var paths:  MutableList<Polyline> = ArrayList()
    private var previousPosition: PositionDto? = null
    private var locationMarkers: MutableList<Marker> = ArrayList()
    private var mapLoaded = false
    private lateinit var deviceId: UUID
    private lateinit var sse: ServerSentEvent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        networkManager = GeneralHelper.getNetworkManager(this)
        deviceId = UUID.randomUUID()
    }

    override fun onResume() {
        super.onResume()
    }

    fun onReceiveUpdatesClick(view: View) {
        getLocations()
    }

    fun onStopUpdatesClick(view: View) {
        sse.close()
        clear()
    }

    fun onExitMapClick(view: View) {
        finish()
    }

    private fun getLocations() {
        loadPositions()
        subscribeToServer()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap1 = googleMap

        // Add a marker in Sydney and move the camera
        val sunningHill = LatLng(-26.032207, 28.074862)
        googleMap1.animateCamera(CameraUpdateFactory.newLatLngZoom(sunningHill, 6F))
    }

    private fun loadPositions() {
        compositeDisposable?.add(
            networkManager.getPositions(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.i(TAG, "loadPositions success")
                    handlePositions(it)
                }, { e ->
                    val error = processError(e)
                    error.printStackTrace()
                    Log.i(TAG, "loadPositions Error: ${error.message}")
                })
        )
    }

    private fun subscribeToServer() {
        val url = getString(R.string.text_base_url) + "track/open/$id/register?clientId=${deviceId.toString()}"

        val request = Request.Builder()
            .url(url)
            .build()
        //var okSse = OkSse(GeneralHelper.provideOkHttpClientBuilder().build())
        val okSse = OkSse()
        sse = okSse.newServerSentEvent(request, listener)

    }

    private val listener = object : ServerSentEvent.Listener {
        override fun onOpen(sse: ServerSentEvent?, response: Response?) {
            Log.i(TAG, "ServerSentEvent.Listener onOpen")
        }

        override fun onRetryTime(sse: ServerSentEvent?, milliseconds: Long): Boolean {
            Log.i(TAG, "ServerSentEvent.Listener onRetryTime")
            return true
        }

        override fun onComment(sse: ServerSentEvent?, comment: String?) {
            Log.i(TAG, "ServerSentEvent.Listener onComment")
        }

        override fun onRetryError(sse: ServerSentEvent?, throwable: Throwable?, response: Response?): Boolean {
            Log.i(TAG, "ServerSentEvent.Listener onRetryError")
            throwable?.printStackTrace()
            return true
        }

        override fun onPreRetry(sse: ServerSentEvent?, originalRequest: Request?): Request {
            Log.i(TAG, "ServerSentEvent.Listener onPreRetry")
            return originalRequest!!
        }

        override fun onMessage(sse: ServerSentEvent?, id: String?, event: String?, message: String?) {
            Log.i(TAG, "ServerSentEvent.Listener onMessage")
            Log.i(TAG, "SSE Id: $id")
            Log.i(TAG, "SSE Event: $event")
            Log.i(TAG, "SSE Message: $message")
            event?.let {
                when {
                    it.contains("pos") -> message?.let { messageText ->
                        for(line in messageText.split('\n')) {
                            //val jobj = JSONObject(line)
                            val listType = object : TypeToken<List<PositionDto>>() {}.type
                            val positions = GeneralHelper.getObjectFromJsonArray(line, listType) as List<PositionDto>
                            runOnUiThread { handlePositions(positions) }

                        }
                    }
                    it.contains("clear") -> runOnUiThread {clear()}
                    else -> Log.i(TAG, "Unknown Event. Did I Subscribe to this sh*t? ")
                }
            }

        }

        override fun onClosed(sse: ServerSentEvent?) {
            Log.i(TAG, "ServerSentEvent.Listener onClosed")


        }

    }

    private fun clear() {
        Log.i(TAG, "Clear")
        sse.close()
        locationMarkers.forEach { it.remove() }
        locationMarkers = ArrayList()
        previousPosition = null
        currentLocationMarker?.remove()
        currentLocationMarker = null
        locationAccuracyCircle?.remove()
        locationAccuracyCircle = null
        paths.forEach { it.remove() }
        paths = ArrayList()

    }

    private fun handlePositions(it: List<PositionDto>?) {
        it?.let { positions ->
            for (position in positions) {
                handlePosition(position)
            }

            if (positions.isNotEmpty()) {
                val lastPos = positions[positions.size - 1]
                if (lastPos.latitude != null && lastPos.longitude != null) {
                    val latLng = LatLng(lastPos.latitude!!, lastPos.longitude!!)

                    googleMap1.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
                }
            }
        }
    }

    private fun handlePosition(position: PositionDto) {
        if (position.latitude != null && position.longitude != null) {
            val latLng = LatLng(position.latitude!!, position.longitude!!)
            val iconGold = BitmapDescriptorFactory.fromResource(R.drawable.circle_gold)
            val iconGreen = BitmapDescriptorFactory.fromResource(R.drawable.circle_green)

            if (currentLocationMarker == null) {
                currentLocationMarker = googleMap1.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(iconGold)
                )

                locationAccuracyCircle = googleMap1.addCircle(
                    CircleOptions()
                        .center(latLng)
                        .fillColor(R.color.colorAccentTransparent50)
                        .strokeColor(R.color.colorAccentTransparent50)
                        .radius(position.accuracy ?: 0.0)
                )
            } else {
                currentLocationMarker?.position = latLng
                locationAccuracyCircle?.center = latLng
                locationAccuracyCircle?.radius = position.accuracy ?: 0.0
            }

            if (previousPosition != null && previousPosition!!.latitude != null && previousPosition!!.longitude != null) {
                val previousLatLng = LatLng(previousPosition!!.latitude!!, previousPosition!!.longitude!!)
                locationMarkers.add(
                    googleMap1.addMarker(
                        MarkerOptions()
                            .position(previousLatLng)
                            .icon(iconGreen)
                    )
                )

                if (locationMarkers.size > 100) {
                    val removedMarker = locationMarkers.removeAt(locationMarkers.size - 1)
                    removedMarker.remove()
                }

                //Add Line from prev position to new position
                paths.add(
                googleMap1.addPolyline(PolylineOptions()
                    .color(R.color.colorPrimaryTransparent50)
                    .add(previousLatLng, latLng))
                )

            } else {
                googleMap1.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
            }

            /*if (path == null) {
                path = googleMap1.addPolyline(
                    PolylineOptions()
                        .color(R.color.colorPrimaryTransparent50)
                )
            }*/

            //path.

            if (paths.size > 100) {
                val removedPath = paths.removeAt(paths.size - 1)
                removedPath.remove()
            }

            previousPosition = position
        }
    }

    private fun processError(error: Throwable): Throwable {
        Log.i(TAG, "Error: " + GeneralHelper.getJsonFromObject(error))
        return if (error is HttpException) {
            Log.i(TAG, "This is HttpException")
            ServerException(error.code(), error.response().errorBody()?.string(), error.cause)
        } else error
    }
}
