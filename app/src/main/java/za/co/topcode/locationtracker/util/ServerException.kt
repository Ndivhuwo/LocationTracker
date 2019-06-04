package za.co.topcode.locationtracker.util

class ServerException (errorCode: Int, message: String?, override val cause: Throwable?): Throwable(message, cause) {
    constructor(errorCode: Int, errorMessage: String?):
            this(errorCode, errorMessage, null)

    constructor(errorCode: Int):
            this(errorCode, null, null)

}