package com.oussama.portfolio.data.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize


@JsonClass(generateAdapter = true)
@Parcelize
data class AboutMeModel(
    @Json(name = "description")
    var description: String = "",
    @Json(name = "media")
    var media: List<MediaModel> = listOf()
) : Parcelable

