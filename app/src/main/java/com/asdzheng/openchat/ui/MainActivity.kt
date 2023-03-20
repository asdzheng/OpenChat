package com.asdzheng.openchat.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.asdzheng.openchat.R
import com.asdzheng.openchat.databinding.ActivityMainBinding
import com.asdzheng.openchat.db.RoomHelper
import com.asdzheng.openchat.db.model.Chat
import com.asdzheng.openchat.db.model.ChatMessage
import com.asdzheng.openchat.net.OpenClient
import com.asdzheng.openchat.ui.adapter.MessageAdapter
import com.asdzheng.openchat.util.JsonUtil
import com.asdzheng.openchat.util.DataHelper
import com.asdzheng.openchat.util.PreferencesManager
import com.bluewhaleyt.common.DynamicColorsUtil
import com.bluewhaleyt.common.IntentUtil
import com.bluewhaleyt.component.snackbar.SnackbarUtil
import com.unfbx.chatgpt.entity.chat.ChatCompletion
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse
import com.unfbx.chatgpt.entity.chat.Message.Role
import com.unfbx.chatgpt.sse.ConsoleEventSourceListener
import kotlinx.coroutines.cancel
import kotlinx.coroutines.newSingleThreadContext
import lombok.SneakyThrows
import okhttp3.Response
import okhttp3.sse.EventSource
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private val threadPool = newSingleThreadContext("message")

    private lateinit var adapter: MessageAdapter
    private var mChatMessages: MutableList<ChatMessage>? = null
    private lateinit var chat: Chat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
    }

    override fun onStart() {
        super.onStart()
        setupChatGPT()
    }

    private fun setupChatGPT() {
        if (PreferencesManager.getOpenAIAPIKey().equals("")) {
            SnackbarUtil.makeSnackbar(this, getString(R.string.api_key_missing))
            binding.layoutMessageInputContainer.visibility = View.GONE
            binding.rvChatList.visibility = View.GONE
            IntentUtil.intent(this, SettingsActivity::class.java)
        } else {
            binding.layoutMessageInputContainer.visibility = View.VISIBLE
            binding.rvChatList.visibility = View.VISIBLE
        }
    }

    private fun initialize() {
        chat = DataHelper.generateDefaultChat(getString(R.string.casual_chat), getString(R.string.casual_chat))
        loadHistoryData()
        setupChatList()
        binding.btnSend.setOnClickListener { sendMessage() }
        val gd = GradientDrawable()
        val dynamic = DynamicColorsUtil(this)
        gd.setColor(dynamic.colorPrimaryContainer)
        gd.alpha = 100
        gd.cornerRadius = 100f
        binding.etMessage.background = gd
    }

    private fun loadHistoryData() {
        threadPool.executor.execute {
            mChatMessages = RoomHelper.getInstance().chatMessageDao().queryAll()
            runOnUiThread {
                adapter.setMessageList(mChatMessages)
            }
        }
    }

    private fun setupChatList() {
        mChatMessages = CopyOnWriteArrayList()
        adapter = MessageAdapter(mChatMessages)
        val layoutManager = LinearLayoutManager(this);
        layoutManager.stackFromEnd = true;
        binding.rvChatList.layoutManager = layoutManager;
        binding.rvChatList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
            getDrawable(R.drawable.divider)?.let { setDrawable(it) }
        })

        binding.rvChatList.adapter = adapter;
        adapter.setMessageList(mChatMessages);
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> IntentUtil.intent(this, SettingsActivity::class.java)
            R.id.menu_clear_messages -> {
                threadPool.cancel()
                mChatMessages = mutableListOf()
                adapter.setMessageList(mChatMessages)
                RoomHelper.getInstance().chatMessageDao().deleteAll(chat.title!!)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendMessage() {
        val userInput = binding.etMessage.text.toString()
        if (!TextUtils.isEmpty(userInput)) {
            threadPool.executor.execute {
                val userInputMessage = generateMessage(userInput, Role.USER.name)
                RoomHelper.getInstance().chatMessageDao().insert(userInputMessage)
                runOnUiThread {
                    addMessage(userInputMessage)
                    binding.etMessage.setText("")
                }
                val countDownLatch = CountDownLatch(1)
                val eventSourceListener = object : ConsoleEventSourceListener() {
                    var chatMessage: ChatMessage? = null

                    override fun onOpen(eventSource: EventSource, response: Response) {
                        super.onOpen(eventSource, response)
                        chatMessage = generateMessage("", Role.ASSISTANT.name)
                    }

                    override fun onEvent(
                        eventSource: EventSource,
                        id: String?,
                        type: String?,
                        data: String
                    ) {
                        super.onEvent(eventSource, id, type, data)
                        val response = JsonUtil.parse(data, ChatCompletionResponse::class.java)
                        val choice = response?.choices?.firstOrNull()
                        if (choice?.finishReason.isNullOrEmpty()) {
                            choice?.delta?.content?.let {
                                chatMessage?.apply {
                                    content += it
                                    content.trim()
                                    runOnUiThread {
                                        addMessage(this)
                                    }
                                }
                            }
                        } else {
                            chatMessage?.apply {
                                RoomHelper.getInstance().chatMessageDao().insert(this)
                            }
                            countDownLatch.countDown()
                        }
                    }

                    override fun onClosed(eventSource: EventSource) {
                        super.onClosed(eventSource)
                        countDownLatch.countDown()
                    }

                    @SneakyThrows
                    override fun onFailure(
                        eventSource: EventSource,
                        t: Throwable?,
                        response: Response?
                    ) {
                        super.onFailure(eventSource, t, response)
                        countDownLatch.countDown()
                    }
                }

                val chatCompletion =
                    ChatCompletion.builder()
                        .messages(DataHelper.getMessageContext(chat.prompt!!, chat.title!!))
                        .model(PreferencesManager.getOpenAIModel())
                        .maxTokens(PreferencesManager.getOpenAIMaxTokens().toInt())
                        .stream(false)
                        .temperature(PreferencesManager.getOpenAITemperature().toDouble())
                        .build()
                OpenClient.streamApi?.streamChatCompletion(chatCompletion, eventSourceListener)

                try {
                    countDownLatch.await()

                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun addMessage(chatMessage: ChatMessage) {
        mChatMessages?.apply {
            if (!contains(chatMessage)) {
                add(chatMessage)
                adapter.notifyItemInserted(size - 1)
                binding.rvChatList.smoothScrollToPosition(size - 1)
            } else {
                notifyLastMessage()
            }
        }
    }

    private fun notifyLastMessage() {
        runOnUiThread {
            mChatMessages?.apply {
                if (isNotEmpty()) {
                    adapter.notifyItemChanged(size - 1)
                }
            }
        }
    }


    private fun generateMessage(
        message: String,
        role: String
    ): ChatMessage {
        val chatMessage = ChatMessage(
            message, role,
            Calendar.getInstance().timeInMillis.toDouble(),
            getString(R.string.casual_chat)
        )
        chatMessage.model = PreferencesManager.getOpenAIModel()
        return chatMessage
    }

}