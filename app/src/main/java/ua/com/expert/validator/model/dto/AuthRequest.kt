package ua.com.expert.validator.model.dto

import com.google.gson.annotations.SerializedName

class AuthRequest(@field:SerializedName("CardCode")
                  var cardCode: String?,
                  @field:SerializedName("TermID")
                  var termId: String?)
