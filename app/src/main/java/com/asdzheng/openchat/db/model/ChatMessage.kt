package com.asdzheng.openchat.db.model

import android.icu.util.Calendar
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.asdzheng.openchat.util.PreferencesManager

@Entity(tableName = "ChatMessage")
class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var content: String = ""
    var model: String
    //chat group title
    var title: String? = null
    var role: String
    var time = 0.0


    constructor(content: String, role: String, time: Double, title: String?) {
        this.content = content
        this.role = role
        this.time = time
        this.title = title
        this.model = PreferencesManager.getOpenAIModel()
    }
}