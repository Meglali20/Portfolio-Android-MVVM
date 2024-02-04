package com.oussama.portfolio.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oussama.portfolio.data.local.room.entities.ProjectEntity

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE title = :title AND lang = :lang LIMIT 1")
    fun getProject(title: String, lang: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProject(projectEntity: ProjectEntity): Long

    @Delete
    fun deleteProject(projectEntity: ProjectEntity): Int
}