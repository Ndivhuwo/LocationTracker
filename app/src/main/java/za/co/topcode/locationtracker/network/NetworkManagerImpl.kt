package za.co.topcode.locationtracker.network

import io.reactivex.Observable
import za.co.topcode.locationtracker.model.PositionDto
import za.co.topcode.locationtracker.network.contract.NetworkManager
import za.co.topcode.locationtracker.network.service.NetworkService

class NetworkManagerImpl constructor(private val networkService: NetworkService): NetworkManager{

    override fun updateLocation(activityId: String?, positionDto: PositionDto?): Observable<Boolean> {
        return networkService.updateLocation(activityId, positionDto)
    }

    override fun clearLocations(activityId: String?): Observable<Boolean> {
        return networkService.clearLocations(activityId)
    }

    override fun getPositions(activityId: String?): Observable<List<PositionDto>> {
        return networkService.getPositions(activityId)
    }
}