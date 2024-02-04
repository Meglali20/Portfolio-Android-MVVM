package com.oussama.portfolio.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oussama.portfolio.data.local.room.entities.ContactEntity

@Dao
interface ContactDao {
    @Query("SELECT * FROM contact")
    fun getContacts(): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContact(contactEntity: ContactEntity): Long

    @Delete
    fun deleteContact(contactEntity: ContactEntity): Int

    @Delete
    fun deleteContacts(contactEntities: List<ContactEntity>): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContacts(contactEntities: List<ContactEntity>): List<Long>
}