package za.co.topcode.locationtracker.util

import android.content.Context
import android.location.LocationManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import za.co.topcode.locationtracker.BuildConfig
import za.co.topcode.locationtracker.R
import za.co.topcode.locationtracker.network.NetworkManagerImpl
import za.co.topcode.locationtracker.network.contract.NetworkManager
import za.co.topcode.locationtracker.network.service.NetworkService
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit

class GeneralHelper {

    companion object {
        fun isLocationEnabled(context: Context): Boolean {
            var locationManager: LocationManager? = null
            var gps_enabled = false
            var network_enabled = false

            //Check GPS provider
            if (locationManager == null)
                locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            try {
                gps_enabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)?: false
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //Check network provider
            try {
                network_enabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)?: false
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return gps_enabled || network_enabled
        }

        fun provideOkHttpClientBuilder(): OkHttpClient.Builder {

            val clientBuilder = OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            clientBuilder.addInterceptor(loggingInterceptor)

            //Global Header
            val headerContentTypeInterceptor = Interceptor { chain ->
                var request: Request = chain.request()
                val headers = request.headers().newBuilder().add("Content-Type", "application/json").build()
                request = request.newBuilder().headers(headers).build()
                chain.proceed(request)
            }
            clientBuilder.addInterceptor(headerContentTypeInterceptor)

            return clientBuilder
        }

        fun provideNetworkService(context: Context, gson: Gson, clientBuilder: OkHttpClient.Builder): NetworkService {

            val retrofit = Retrofit.Builder()
                .client(clientBuilder.build())
                .baseUrl(context.getString(R.string.text_base_url))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            return retrofit.create(NetworkService::class.java)
        }

        fun provideGson(): Gson {
            return GsonBuilder()
                .registerTypeAdapter(Int::class.java, IntegerTypeAdapter())
                .registerTypeAdapter(Date::class.java, MyDateTypeAdapter())
                .serializeNulls()
                .setLenient()
                .create()
        }

        fun getJsonFromObject(`object`: Any): String {
            return provideGson().toJson(`object`)
        }

        fun getObjectFromJson(json: String, classType: Class<*>): Any {
            return provideGson().fromJson(json, classType)
        }

        fun getObjectFromJsonArray(jsonArray: String, type: Type): Any {
            return provideGson().fromJson(jsonArray, type)
        }

        fun getNetworkManager(context: Context): NetworkManager{
            return NetworkManagerImpl(provideNetworkService(context, provideGson(), provideOkHttpClientBuilder()))
        }
    }
}