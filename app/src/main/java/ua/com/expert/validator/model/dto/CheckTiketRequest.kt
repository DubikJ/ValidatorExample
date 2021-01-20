package ua.com.expert.validator.model.dto

import com.google.gson.annotations.SerializedName

class CheckTiketRequest(@field:SerializedName("QRCode")
                        val qrCode : String,
                        @field:SerializedName("PlaceGroupID")
                        val idPlace : Int,
                        @field:SerializedName("CinemaSessionID")
                        val idSession : Int)