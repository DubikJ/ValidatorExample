package ua.com.expert.validator.sync

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import ua.com.expert.validator.common.Consts
import ua.com.expert.validator.common.Consts.LOGOUT_PATTERN_URL
import ua.com.expert.validator.common.Consts.QR_CODE_VALIDATE_PATTERN_URL
import ua.com.expert.validator.common.Consts.TOKEN_HEADER
import ua.com.expert.validator.common.Consts.VALIDATE_LICENSE_URL
import ua.com.expert.validator.model.dto.*
import ua.com.expert.validator.model.dto.*
import ua.com.expert.validator.model.dto.*


interface SyncService {

    @POST(Consts.AUTHENTICATE_PATTERN_URL)
    fun authenticate(@Body request: AuthRequest): Call<AuthResponse>

    @POST(Consts.GET_CINEMA_PLACE_PATTERN_URL)
    fun getCinemaPlaceGroup(@Header(TOKEN_HEADER) headerToken : String): Call<CinemaPlaceResponse>

    @POST(Consts.GET_CINEMA_SESSIONS_PATTERN_URL)
    fun getCinemaSessions(@Header(TOKEN_HEADER) headerToken : String,
                          @Body request: CinemaSessionRequest): Call<CinemaSessionResponse>

    @POST(Consts.CHECK_TICKET_PATTERN_URL)
    fun checkTicket(@Header(TOKEN_HEADER) headerToken : String,
                    @Body request: CheckTiketRequest): Observable<DownloadResponse>

    @POST(VALIDATE_LICENSE_URL)
    fun validateLicense(@Header(TOKEN_HEADER) headerToken : String): Observable<LicenseResponse>

    @POST(LOGOUT_PATTERN_URL)
    fun logOut(@Header(TOKEN_HEADER) headerToken : String): Observable<DownloadResponse>

    @POST(QR_CODE_VALIDATE_PATTERN_URL)
    fun qrCodeValidate(@Header(TOKEN_HEADER) headerToken : String,
                       @Body request: ValidateRequest): Observable<ValidateResponse>

}