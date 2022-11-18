package com.cindaku.holanear.db.dao

import androidx.room.*
import com.cindaku.holanear.db.entity.Contact

@Dao
interface ContactDao {
    @Query("SELECT * FROM contact")
    fun all(): List<Contact>
    @Query("SELECT * FROM contact WHERE phone like :phone ")
    fun get(phone: String): List<Contact>
    @Insert
    fun insert(contact: Contact)
    @Update
    fun update(contact: Contact): Int
    @Delete
    fun delete(contact: Contact): Int
    @Query("select contact.* from contact where phone=:phone")
    fun getByPhone(phone: String): Contact?
    @Query("select contact.* from contact where jId=:jid")
    fun getByJid(jid: String): Contact?
    @Query("select contact.* from contact where contact_id=:id")
    fun getById(id: Int): Contact?
    @Query("delete from contact where :id")
    fun deleteByID(id: Int): Int
}