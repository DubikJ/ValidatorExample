package ua.com.expert.validator.model.dto

import com.google.gson.annotations.SerializedName

class CinemaPlaceResponse (error: String?,
                           errorCode: Int,
                            @field:SerializedName("CinemaPlaceGroups")
                            val cinemaPlaceGroups: List<CinemaPlace>?) : DownloadResponse(error, errorCode) {

    data class Builder(
            var error: String? = null,
            var errorCode: Int = 0,
            var cinemaPlaceGroups: List<CinemaPlace>? = null) {

        fun error(error: String) = apply { this.error = error }
        fun errorCode(errorCode: Int) = apply { this.errorCode = errorCode }
        fun cinemaPlaceGroups(cinemaPlaceGroups: List<CinemaPlace>) = apply { this.cinemaPlaceGroups = cinemaPlaceGroups }

        fun build() = CinemaPlaceResponse(error, errorCode, cinemaPlaceGroups)
    }
}