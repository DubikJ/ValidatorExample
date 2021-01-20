package ua.com.expert.validator.model.dto

import com.google.gson.annotations.SerializedName

class AuthResponse(error: String?,
                   errorCode: Int,
                   @field:SerializedName("UserID")
                   val userId: Int,
                   @field:SerializedName("FirstName")
                   val firstName: String?,
                   @field:SerializedName("LastName")
                   val lastName: String?,
                   @field:SerializedName("MiddleName")
                   val middleName: String?,
                   @field:SerializedName("BirthDate")
                   val birthDate: String?,
                   @field:SerializedName("Sex")
                   val sex: String?,
                   @field:SerializedName("Valid")
                   val valid: String?,
                   @field:SerializedName("Token")
                   val token: String?,
                   @field:SerializedName("LicAddr")
                   val licAddr: String?,
                   @field:SerializedName("LicPort")
                   val licPort: String?,
                   @field:SerializedName("BroadcastMobileUdpPort")
                   val broadcastMobileUdpPort: Int?,
                   @field:SerializedName("SessionID")
                   val sessionID: Int?,
                   @field:SerializedName("SessionNumber")
                   val sessionNumber: Int?,
                   @field:SerializedName("SessionTermID")
                   val sessionTermID: Int?,
                   @field:SerializedName("SessionOpened")
                   val sessionOpened: String?,
                   @field:SerializedName("PlaceGroupCount")
                   val placeGroupCount: Int?,
                   @field:SerializedName("NameTerminal")
                   val nameTerminal: String?) : DownloadResponse(error, errorCode){


    data class Builder(
            var error: String? = null,
            var errorCode: Int = 0,
            var userId: Int = 0,
            var firstName: String? = null,
            var lastName: String? = null,
            val middleName: String? = null,
            val birthDate: String? = null,
            val sex: String? = null,
            val valid: String? = null,
            val token: String? = null,
            val licAddr: String? = null,
            val licPort: String? = null,
            val broadcastMobileUdpPort: Int? = null,
            val sessionID: Int? = null,
            val sessionNumber: Int? = null,
            val sessionTermID: Int? = null,
            val sessionOpened: String? = null,
            val placeGroupCount: Int? = null,
            val nameTerminal: String? = null) {

        fun error(error: String) = apply { this.error = error}
        fun errorCode(errorCode: Int) = apply { this.errorCode = errorCode}
        fun userId(userId: Int) = apply { this.userId = userId }
        fun firstName(firstName: String) = apply { this.firstName = firstName }
        fun lastName(lastName: String) = apply { this.lastName = lastName }

        fun build() = AuthResponse(error, errorCode, userId, firstName, lastName, middleName, birthDate,
                sex, valid, token, licAddr, licPort, broadcastMobileUdpPort, sessionID,
                sessionNumber, sessionTermID, sessionOpened, placeGroupCount, nameTerminal)
    }
}
