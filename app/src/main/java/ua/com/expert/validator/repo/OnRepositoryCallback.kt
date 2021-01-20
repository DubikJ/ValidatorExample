package ua.com.expert.validator.repo

import android.webkit.DownloadListener
import ua.com.expert.validator.model.dto.AuthResponse
import ua.com.expert.validator.model.dto.CinemaPlaceResponse
import ua.com.expert.validator.model.dto.CinemaSessionResponse
import ua.com.expert.validator.model.dto.DownloadResponse

interface OnRepositoryCallback {

    interface Auth {

        fun onResponse(response: AuthResponse)

        fun onError(errorCode: Int)

        fun onFailure(t: Throwable)
    }

    interface Places {

        fun onResponse(response: CinemaPlaceResponse)

        fun onError(errorCode: Int)

        fun onFailure(t: Throwable)
    }

    interface Sessions {

        fun onResponse(response: CinemaSessionResponse)

        fun onError(errorCode: Int)

        fun onFailure(t: Throwable)
    }

}