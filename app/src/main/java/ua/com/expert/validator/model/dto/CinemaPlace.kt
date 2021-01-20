package ua.com.expert.validator.model.dto

import com.google.gson.annotations.SerializedName

class CinemaPlace (@field:SerializedName("ID")
                    val id: Int,
                  @field:SerializedName("Name")
                    val name: String?,
                  @field:SerializedName("PlaceCount")
                    val placeCount: Int)
