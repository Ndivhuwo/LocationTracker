package za.co.topcode.locationtracker.network.contract

import io.reactivex.Observable
import za.co.topcode.locationtracker.model.PositionDto

interface NetworkManager {
    fun updateLocation(activityId: String?, positionDto: PositionDto?): Observable<Boolean>
    fun clearLocations(activityId: String?) : Observable<Boolean>
}