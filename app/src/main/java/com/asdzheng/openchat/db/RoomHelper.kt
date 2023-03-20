package com.asdzheng.openchat.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.asdzheng.openchat.App
import com.asdzheng.openchat.db.dao.ChatDao
import com.asdzheng.openchat.db.dao.ChatMessageDao
import com.asdzheng.openchat.db.model.Chat
import com.asdzheng.openchat.db.model.ChatMessage


/**
 * @author zhengjb
 * @date on 2023/3/19
 */
@Database(entities = [Chat::class, ChatMessage::class], version = 1)
abstract class RoomHelper : RoomDatabase(){
    //创建DAO的抽象类
    abstract fun chatDao(): ChatDao

    //创建DAO的抽象类
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        private const val DATABASE_NAME = "open_chat.db"
        @Volatile
        private var databaseInstance: RoomHelper? = null

        @Synchronized
        open fun getInstance(): RoomHelper {
            if (databaseInstance == null) {
                databaseInstance = Room.databaseBuilder(App.getContext(), RoomHelper::class.java,DATABASE_NAME)
                    .allowMainThreadQueries()//允许在主线程操作数据库，一般不推荐；设置这个后主线程调用增删改查不会报错，否则会报错
                    .build()
            }
            return databaseInstance!!
        }
    }
}