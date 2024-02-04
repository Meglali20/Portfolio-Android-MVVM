package com.oussama.portfolio.data.local.room.converters

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class StringListConverter {
    private val moshi: Moshi = Moshi.Builder().build()
    private val listStringAdapter: JsonAdapter<List<String>> = moshi.adapter(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    @TypeConverter
    fun fromList(list: List<String>): String {
        return listStringAdapter.toJson(list)
    }

    @TypeConverter
    fun toList(jsonString: String): List<String> {
        return listStringAdapter.fromJson(jsonString) ?: emptyList()
    }
}

