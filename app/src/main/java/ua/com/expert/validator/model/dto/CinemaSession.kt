package ua.com.expert.validator.model.dto

import com.google.gson.annotations.SerializedName

class CinemaSession(@field:SerializedName("ID")
                    val id: Int,
                    @field:SerializedName("Start")
                    val dateStart: String?,
                    @field:SerializedName("Finish")
                    val dateFinish: String?,
                    @field:SerializedName("FilmName")
                    val filmName: String?,
                    @field:SerializedName("FilmID")
                    val filmID: Int)
