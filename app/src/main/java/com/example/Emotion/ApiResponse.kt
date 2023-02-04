package com.example.Emotion

enum class NetworkStatus {
    LOADING, SUCCESS, ERROR, COMPLETED, EXPIRE
}
class ApiResponse private constructor(
val status: NetworkStatus,
var t: Any?,
val error: CustomError?
) {

    companion object {
        fun loading(): ApiResponse {
            return ApiResponse(NetworkStatus.LOADING, null, null)
        }

        fun success(t: Any?): ApiResponse {
            return ApiResponse(NetworkStatus.SUCCESS, t, null)
        }

        fun error(error: CustomError?): ApiResponse {
            return ApiResponse(NetworkStatus.ERROR, null, error)
        }

        fun complete(): ApiResponse {
            return ApiResponse(NetworkStatus.COMPLETED, null, null)
        }

        fun expire(error: CustomError?): ApiResponse {
            return ApiResponse(NetworkStatus.EXPIRE, null, error)
        }
    }

}