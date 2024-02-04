package com.oussama.portfolio.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.oussama.portfolio.data.models.ProjectModel
import com.oussama.portfolio.data.local.room.converters.ProjectListConverter

@TypeConverters(ProjectListConverter::class)
@Entity(tableName = "portfolio")
data class PortfolioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val projects: List<ProjectModel>,
    val lang: String
)

