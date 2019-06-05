package za.co.topcode.locationtracker.network.service

import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path
import za.co.topcode.locationtracker.model.PositionDto

interface NetworkService {

    @POST("track/open/{activityId}/updateLocation")
    fun updateLocation(@Path("activityId") activityId: String?, @Body positionDto: PositionDto?): Observable<Boolean>

    @DELETE("track/open/{activityId}/clear")
    fun clearLocations(@Path("activityId") activityId: String?) : Observable<Boolean>

    @POST("track/open/{activityId}/positions")
    fun getPositions(@Path("activityId") activityId: String?): Observable<List<PositionDto>>
}