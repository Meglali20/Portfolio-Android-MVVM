package com.oussama.portfolio.data.local.room.converters

import androidx.room.TypeConverter
import com.oussama.portfolio.data.models.MediaModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types


class MediaListConverter {
    private val moshi: Moshi = Moshi.Builder().build()
    private val listMediaModelAdapter: JsonAdapter<List<MediaModel>> = moshi.adapter(
        Types.newParameterizedType(List::class.java, MediaModel::class.java)
    )

    @TypeConverter
    fun fromList(list: List<MediaModel>): String {
        return listMediaModelAdapter.toJson(list)
    }

    @TypeConverter
    fun toList(jsonString: String): List<MediaModel> {
        return listMediaModelAdapter.fromJson(jsonString) ?: emptyList()
    }
}

