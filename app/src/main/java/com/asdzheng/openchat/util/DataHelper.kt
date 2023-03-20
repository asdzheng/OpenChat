package com.asdzheng.openchat.util

import android.util.Log
import com.asdzheng.openchat.R
import com.asdzheng.openchat.db.RoomHelper
import com.asdzheng.openchat.db.model.Chat
import com.unfbx.chatgpt.entity.chat.Message


/**
 * @author zhengjb
 * @date on 2023/3/19
 */
object DataHelper {
     fun getMessageContext( prompt: String, title:String) : List<Message> {
        val contextMessages = mutableListOf<Message>()
        val systemMessage = Message.builder().role(Message.Role.SYSTEM).content(prompt).build()
        val latestMessages = RoomHelper.getInstance().chatMessageDao().queryByTitleContext(title, 6)
        val messages = latestMessages.map{
            Message.builder().role(Message.Role.valueOf(it.role)).content(it.content).build()
        }
         contextMessages.add(systemMessage)
         contextMessages.addAll(messages.reversed())

         Log.w("data", contextMessages.joinToString ( prefix = "[",
             separator = "\n\n",
             postfix = "]")
         )
        return contextMessages
    }

    fun generateDefaultChat(title: String, prompt: String): Chat {
        return Chat().apply {
            this.id = 1
            this.title = title
            this.prompt = prompt
            this.icon = R.drawable.ic_outline_token_24
        }
    }
}