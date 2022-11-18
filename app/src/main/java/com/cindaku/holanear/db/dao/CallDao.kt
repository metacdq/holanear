package com.cindaku.holanear.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cindaku.holanear.db.entity.Call
import com.cindaku.holanear.db.entity.CallMember
import com.cindaku.holanear.db.entity.CallWithDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Query("select * from call inner join contact as caller on caller.contact_id=call.contact_id inner join callmember on callmember.call_id=call.call_id inner join contact as member on member.contact_id=callmember.contact_id")
    fun allCall(): Flow<List<CallWithDetail>>
    @Insert
    fun insert(call: Call): Long
    @Insert
    fun insertMember(call: CallMember)
    @Update
    fun update(call: Call)
    @Query("select * from call where contact_id=:contact_id and is_ended=0")
    fun findCallByContactId(contact_id: Int): Call?
}