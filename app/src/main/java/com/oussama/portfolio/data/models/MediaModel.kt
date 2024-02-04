package com.oussama.portfolio.data.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class MediaModel(
    @Json(name = "imageUrl")
    val imageUrl: String = ""
) : Parcelable