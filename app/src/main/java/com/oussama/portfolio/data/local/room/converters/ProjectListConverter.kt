package com.oussama.portfolio.data.local.room.converters

import androidx.room.TypeConverter
import com.oussama.portfolio.data.models.MediaModel
import com.oussama.portfolio.data.models.ProjectModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class ProjectListConverter {
    private val moshi: Moshi = Moshi.Builder().build()

    private val listProjectModelAdapter: JsonAdapter<List<ProjectModel>> = moshi.adapter(
        Types.newParameterizedType(List::class.java, ProjectModel::class.java)
    )

    private val listMediaModelAdapter: JsonAdapter<List<MediaModel>> = moshi.adapter(
        Types.newParameterizedType(List::class.java, MediaModel::class.java)
    )

    @TypeConverter
    fun fromProjectList(projectList: List<ProjectModel>): String {
        return listProjectModelAdapter.toJson(projectList)
    }

    @TypeConverter
    fun toProjectList(jsonString: String): List<ProjectModel> {
        return listProjectModelAdapter.fromJson(jsonString) ?: emptyList()
    }

    @TypeConverter
    fun fromMediaList(mediaList: List<MediaModel>): String {
        return listMediaModelAdapter.toJson(mediaList)
    }

    @TypeConverter
    fun toMediaList(jsonString: String): List<MediaModel> {
        return listMediaModelAdapter.fromJson(jsonString) ?: emptyList()
    }
}