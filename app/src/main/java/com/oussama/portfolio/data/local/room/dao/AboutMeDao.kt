package com.oussama.portfolio.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oussama.portfolio.data.local.room.entities.AboutMeEntity

@Dao
interface AboutMeDao {
    @Query("SELECT * FROM aboutMe WHERE lang = :lang LIMIT 1")
    fun getAboutMe(lang: String): AboutMeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAboutMe(aboutMeEntity: AboutMeEntity): Long

    @Delete
    fun deleteAboutMe(aboutMeEntity: AboutMeEntity): Int
}