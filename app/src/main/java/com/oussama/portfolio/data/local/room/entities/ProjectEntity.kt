package com.oussama.portfolio.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.oussama.portfolio.data.models.MediaModel
import com.oussama.portfolio.data.local.room.converters.MediaListConverter

@TypeConverters(MediaListConverter::class)
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String = "",
    val title: String = "",
    val icon: String = "",
    val bannerImage: String = "",
    val preview: String = "",
    val media: List<MediaModel>,
    val lang: String
)