package ua.com.expert.validator.model.dto

import com.google.gson.annotations.SerializedName

class ValidateResponse (error: String?,
                        errorCode: Int,
                        @field:SerializedName("Success") val success: Boolean?,
                        @field:SerializedName("Exists") val exists: Boolean?) : DownloadResponse(error, errorCode){
    data class Builder(
            var error: String? = null,
            var errorCode: Int = 0,
            var success: Boolean? = null,
            var exists: Boolean? = null) {

        fun error(error: String) = apply { this.error = error }
        fun errorCode(errorCode: Int) = apply { this.errorCode = errorCode }
        fun success(success: Boolean) = apply { this.success = success }
        fun exists(exists: Boolean) = apply { this.exists = exists }

        fun build() = ValidateResponse(error, errorCode, success, exists)
    }
}