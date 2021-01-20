package ua.com.expert.validator.model.dto

import com.google.gson.annotations.SerializedName

class LicenseResponse(
        error: String?,
        @field:SerializedName("Owner")
        val owner: String?,
        @field:SerializedName("ValidTo")
        val validTo: String?,
        @field:SerializedName("ValidFrom")
        val validFrom: String?) : DownloadResponse(error)