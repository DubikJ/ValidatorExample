package ua.com.expert.validator.model.dto

import com.google.gson.annotations.SerializedName

class ValidateRequest (@field:SerializedName("QrCode") val qrCode: String)