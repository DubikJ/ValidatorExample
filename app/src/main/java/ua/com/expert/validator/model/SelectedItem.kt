package ua.com.expert.validator.model

import com.google.gson.annotations.SerializedName

class SelectedItem constructor(
        @field:SerializedName("id")
        val id: Int,
        @field:SerializedName("nameScan")
        val nameScan: String,
        @field:SerializedName("selected")
        var selected: Boolean = false) {
}