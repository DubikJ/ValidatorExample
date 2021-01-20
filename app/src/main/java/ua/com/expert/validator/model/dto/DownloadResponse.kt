package ua.com.expert.validator.model.dto

import com.google.gson.annotations.SerializedName

open class DownloadResponse(@field:SerializedName("Error")
                            val error: String?,
                            val errorCode: Int = 0)
