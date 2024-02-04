package com.oussama.portfolio.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.oussama.portfolio.data.local.room.dao.AboutMeDao
import com.oussama.portfolio.data.local.room.dao.ContactDao
import com.oussama.portfolio.data.local.room.dao.ExperienceDao
import com.oussama.portfolio.data.local.room.dao.PortfolioDao
import com.oussama.portfolio.data.local.room.dao.ProjectDao
import com.oussama.portfolio.data.local.room.entities.AboutMeEntity
import com.oussama.portfolio.data.local.room.entities.ContactEntity
import com.oussama.portfolio.data.local.room.entities.ExperienceEntity
import com.oussama.portfolio.data.local.room.entities.PortfolioEntity
import com.oussama.portfolio.data.local.room.entities.ProjectEntity


@Database(
    entities = [AboutMeEntity::class, ExperienceEntity::class, PortfolioEntity::class, ProjectEntity::class, ContactEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PortfolioDatabase : RoomDatabase() {
    abstract fun aboutMeDao(): AboutMeDao
    abstract fun experienceDao(): ExperienceDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun projectDao(): ProjectDao
    abstract fun contactDao(): ContactDao
}