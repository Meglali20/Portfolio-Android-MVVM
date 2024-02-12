package com.oussama.portfolio.data.local.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.oussama.portfolio.data.models.MediaModel
import com.oussama.portfolio.data.local.room.converters.MediaListConverter

@TypeConverters(MediaListConverter::class)
@Entity(tableName = "experience")
data class ExperienceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val media: List<MediaModel>,
    val lang: String
)