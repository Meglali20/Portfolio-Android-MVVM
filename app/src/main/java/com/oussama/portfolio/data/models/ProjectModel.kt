package com.oussama.portfolio.data.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class ProjectModel(
    @Json(name = "description")
    var description: String = "",
    @Json(name = "title")
    var title: String = "",
    @Json(name = "icon")
    var icon: String = "",
    @Json(name = "bannerImage")
    var bannerImage: String = "",
    @Json(name = "preview")
    var preview: String = "",
    @Json(name = "media")
    var media: List<MediaModel> = listOf()
) : Parcelable