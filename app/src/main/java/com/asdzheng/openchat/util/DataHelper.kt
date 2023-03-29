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
    fun getMessageContext(prompt: String, chatId: String): List<Message> {
        val contextMessages = mutableListOf<Message>()
        val systemMessage = Message.builder().role(Message.Role.SYSTEM).content(prompt).build()
        val latestMessages = RoomHelper.getInstance().chatMessageDao().queryByChatIdContext(chatId, 6)
        val messages = latestMessages.map {
            Message.builder().role(Message.Role.valueOf(it.role)).content(it.content).build()
        }
        contextMessages.add(systemMessage)
        contextMessages.addAll(messages.reversed())

        Log.w(
            "data", contextMessages.joinToString(
                prefix = "[",
                separator = "\n\n",
                postfix = "]"
            )
        )
        return contextMessages
    }

    fun generateDefaultChat(context: Context): Chat {
        val chatList = RoomHelper.getInstance().chatDao().queryByType(ChatType.DEFAULT.name);
        return if (chatList.isEmpty()) {
            val defaultChat = Chat().apply {
                this.title = context.getString(R.string.casual_chat)
                this.prompt = context.getString(R.string.casual_prompt)
                this.type = ChatType.DEFAULT.name
            }
            RoomHelper.getInstance().chatDao().insert(defaultChat)
            defaultChat
        } else {
            chatList[0]
        }
    }

    fun getSuggestionsChat(context: Context): MutableList<Chat> {
        var chatList = RoomHelper.getInstance().chatDao().queryByType(ChatType.SUGGESTION.name);
        if (chatList.isEmpty()) {
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
            chatList = mutableListOf(praiseChat, translateChat, codeChat, editorChat)
            RoomHelper.getInstance().chatDao().insert(chatList)
        }
        return chatList
    }

    enum class ChatType {
        DEFAULT, SUGGESTION, USER
    }

}