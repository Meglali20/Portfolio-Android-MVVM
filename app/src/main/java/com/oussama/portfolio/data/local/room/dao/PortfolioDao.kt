package com.oussama.portfolio.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oussama.portfolio.data.local.room.entities.PortfolioEntity


@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio WHERE lang = :lang LIMIT 1")
    fun getPortfolio(lang: String): PortfolioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPortfolio(aboutMeEntity: PortfolioEntity): Long

    @Delete
    fun deletePortfolio(aboutMeEntity: PortfolioEntity): Int
}