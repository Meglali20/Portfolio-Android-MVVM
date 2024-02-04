package com.oussama.portfolio.data.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class ContactModel(
    @Json(name = "title")
    var title: String = "",
    @Json(name = "url")
    var url: String = "",
) : Parcelable