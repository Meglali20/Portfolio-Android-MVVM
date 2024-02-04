package com.oussama.portfolio.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oussama.portfolio.data.local.room.entities.ExperienceEntity

@Dao
interface ExperienceDao {
    @Query("SELECT * FROM experience WHERE lang = :lang LIMIT 1")
    fun getExperience(lang: String): ExperienceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExperience(experienceEntity: ExperienceEntity): Long

    @Delete
    fun deleteExperience(experienceEntity: ExperienceEntity): Int
}