package com.oussama.portfolio.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oussama.portfolio.data.local.room.entities.ContactEntity
import com.oussama.portfolio.data.local.room.entities.ExperienceEntity

@Dao
interface ExperienceDao {
    @Query("SELECT * FROM experience WHERE lang = :lang")
    fun getExperience(lang: String): List<ExperienceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExperience(experienceEntity: ExperienceEntity): Long

    @Delete
    fun deleteExperience(experienceEntity: ExperienceEntity): Int
    @Delete
    fun deleteExperiences(experienceEntities: List<ExperienceEntity>) : Int
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExperiences(experienceEntity: List<ExperienceEntity>): List<Long>

}