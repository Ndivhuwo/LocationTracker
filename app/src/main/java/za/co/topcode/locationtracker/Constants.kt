package za.co.topcode.locationtracker

class Constants {
    companion object {
        //Intent actions
        const val INTENT_ACTION_LOCATION_UPDATE = "za.co.topcode.locationtracker.location.update"

        //BUNDLE EXTRAS
        const val EXTRA_LATITUDE = "EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "EXTRA_LONGITUDE"
        const val EXTRA_BEARING = "EXTRA_BEARING"
        const val EXTRA_SPEED = "EXTRA_SPEED"
        const val EXTRA_ACCURACY = "EXTRA_ACCURACY"
        const val EXTRA_LATITUDE_TEXT = "EXTRA_LATITUDE_TEXT"
        const val EXTRA_LONGITUDE_TEXT = "EXTRA_LONGITUDE_TEXT"
        const val EXTRA_BEARING_TEXT = "EXTRA_BEARING_TEXT"
        const val EXTRA_SPEED_TEXT = "EXTRA_SPEED_TEXT"
        const val EXTRA_ACCURACY_TEXT = "EXTRA_ACCURACY_TEXT"

        //NOTIFICATIONS
        const val NOTIFICATION_ID = 8878
        const val NOTIFICATION_CHANNEL_NAME = "location_tracker_default_channel"
        const val NOTIFICATION_CHANNEL_ID = "default"

        //Request codes
        const val REQUEST_CODE_NOTIFICATION_INTENT = 1

    }
}