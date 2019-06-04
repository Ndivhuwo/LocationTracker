package za.co.topcode.locationtracker.model

data class PositionDto constructor(var accuracy: Double?, var bearing: Double?,
                                   var latitude: Double?, var longitude: Double?,
                                   var speed: Double?, var time: Long?)