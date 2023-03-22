package com.asdzheng.openchat.util

import android.content.Context
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

    fun generateDefaultChat(context: Context): List<Chat> {
        return listOf(Chat().apply {
            this.title = context.getString(R.string.casual_chat)
            this.prompt = context.getString(R.string.casual_chat)
            this.type = ChatType.DEFAULT.name
        })
    }

    fun generateSuggestionsChat(context: Context): MutableList<Chat> {
        val praiseChat = Chat().apply {
            this.title = context.getString(R.string.positive_energy)
            this.prompt = context.getString(R.string.positive_energy_prompt)
            this.type = ChatType.SUGGESTION.name
        }

        val translateChat = Chat().apply {
            this.title = context.getString(R.string.translate_assistant)
            this.prompt = context.getString(R.string.translate_assistant_prompt)
            this.type = ChatType.SUGGESTION.name
        }

        val codeChat = Chat().apply {
            this.title = context.getString(R.string.code_assistant)
            this.prompt = context.getString(R.string.code_assistant_prompt)
            this.type = ChatType.SUGGESTION.name
        }

        val editorChat = Chat().apply {
            this.title = context.getString(R.string.editor_assistant)
            this.prompt = context.getString(R.string.editor_assistant_prompt)
            this.type = ChatType.SUGGESTION.name
        }
        return mutableListOf(praiseChat, translateChat, codeChat, editorChat)
    }

    enum class ChatType {
        DEFAULT, SUGGESTION, USER
    }

}