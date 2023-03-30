package com.asdzheng.openchat.db.dao

import androidx.room.*
import com.asdzheng.openchat.db.model.Chat


/**
 * @author zhengjb
 * @date on 2023/3/19
 */
@Dao
abstract class ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(chat: Chat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(chats: List<Chat>)

    @Delete
    abstract fun delete(chat: Chat)

    @Update
    abstract fun update(chat: Chat)

    @Query("select * from Chat where type IN (:types) order by id DESC")
    abstract fun queryByType(types: List<String>): MutableList<Chat>

    @Query("select * from Chat")
    abstract fun queryAll(): MutableList<Chat>

    @Query("select count(*) from Chat")
    abstract fun chatCount(): Int

    @Query("delete from Chat where title = :title")
    abstract fun deleteAll(title: String)
}