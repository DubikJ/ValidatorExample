package ua.com.expert.validator.model.dto

import com.google.gson.annotations.SerializedName

class CinemaSessionResponse (error: String?,
                             errorCode: Int,
                             @field:SerializedName("CinemaSessions")
                              val cinemaSessions: List<CinemaSession>?) : DownloadResponse(error, errorCode){
    data class Builder(
            var error: String? = null,
            var errorCode: Int = 0,
            var cinemaSessions: List<CinemaSession>? = null) {

        fun error(error: String) = apply { this.error = error }
        fun errorCode(errorCode: Int) = apply { this.errorCode = errorCode }
        fun cinemaSessions(cinemaSessions: List<CinemaSession>) = apply { this.cinemaSessions = cinemaSessions }

        fun build() = CinemaSessionResponse(error, errorCode, cinemaSessions)
    }
}