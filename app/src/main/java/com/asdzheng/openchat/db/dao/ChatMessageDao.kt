package com.asdzheng.openchat.db.dao

import androidx.room.*
import com.asdzheng.openchat.db.model.ChatMessage


/**
 * @author zhengjb
 * @date on 2023/3/19
 */
@Dao
abstract class ChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(message: ChatMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(messages: List<ChatMessage>)

    @Delete
    abstract fun delete(message: ChatMessage)

    @Update
    abstract fun update(message: ChatMessage)

    @Query("select * from ChatMessage where title =:title")
    abstract fun queryByTitle(title: String): MutableList<ChatMessage>

    @Query("select * from ChatMessage where title =:title order by time DESC limit :count")
    abstract fun queryByTitleContext(title: String, count: Int): MutableList<ChatMessage>

    @Query("select * from ChatMessage")
    abstract fun queryAll(): MutableList<ChatMessage>

    @Query("select count(*) from ChatMessage")
    abstract fun chatMessageCount(): Int

    @Query("delete from ChatMessage where title = :title")
    abstract fun deleteAll(title: String)
}