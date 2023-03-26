package com.asdzheng.openchat.ui

import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.asdzheng.openchat.R
import com.asdzheng.openchat.databinding.ActivityChatBinding
import com.asdzheng.openchat.db.RoomHelper
import com.asdzheng.openchat.db.model.Chat
import com.asdzheng.openchat.db.model.ChatMessage
import com.asdzheng.openchat.net.OpenClient
import com.asdzheng.openchat.ui.adapter.MessageAdapter
import com.asdzheng.openchat.util.DataHelper
import com.asdzheng.openchat.util.JsonUtil
import com.asdzheng.openchat.util.PreferencesManager
import com.bluewhaleyt.common.DynamicColorsUtil
import com.bluewhaleyt.common.IntentUtil
import com.bluewhaleyt.component.dialog.DialogUtil
import com.drake.engine.utils.dp
import com.drake.softinput.hasSoftInput
import com.drake.softinput.hideSoftInput
import com.drake.softinput.setWindowSoftInput
import com.drake.softinput.showSoftInput
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

class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding

    private val threadPool = newSingleThreadContext("message")

    private var mChatMessages: MutableList<ChatMessage>? = null
    private var chat: Chat? = null
    private lateinit var adapter: MessageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
    }

    private fun initialize() {
        // 键盘弹出平滑动画
        setWindowSoftInput(float = binding.inputContainer.etMessage, setPadding = true, margin = 10.dp, onChanged = {
            if(hasSoftInput()){
                scrollToEnd(true)
            }
        })
        chat = intent.getSerializableExtra("data") as Chat?
        loadHistoryData()
        setupChatList()
        binding.inputContainer.btnSend.setOnClickListener {
            sendMessage()
        }

        val gd = GradientDrawable()
        val dynamic = DynamicColorsUtil(this)
        gd.setColor(dynamic.colorPrimaryContainer)
        gd.alpha = 100
        gd.cornerRadius = 100f
        binding.inputContainer.etMessage.background = gd
    }

    private fun scrollToEnd(isSmooth: Boolean = true) {
       if( mChatMessages.isNullOrEmpty().not()) {
           if(isSmooth) {
               binding.rvChatList.smoothScrollToPosition(mChatMessages!!.size-1)
           } else {
               binding.rvChatList.scrollToPosition(mChatMessages!!.size-1)
           }
        }
    }

    private fun loadHistoryData() {
        threadPool.executor.execute {
            mChatMessages = chat?.title?.let {
                RoomHelper.getInstance().chatMessageDao().queryByTitle(
                    it
                )
            }
            runOnUiThread {
                adapter.setMessageList(mChatMessages)
                binding.inputContainer.etMessage.showSoftInput()
            }
        }
    }

    private fun setupChatList() {
        mChatMessages = CopyOnWriteArrayList()
        adapter = MessageAdapter(mChatMessages)
        val layoutManager = LinearLayoutManager(this);
        layoutManager.stackFromEnd = true;
        binding.rvChatList.adapter = adapter
        binding.rvChatList.layoutManager = layoutManager;
        binding.rvChatList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
            getDrawable(R.drawable.divider)?.let { setDrawable(it) }
        })
        binding.rvChatList.setItemViewCacheSize(10)

        binding.rvChatList.setOnTouchListener { v, _ ->
            v.clearFocus() // 清除文字选中状态
            hideSoftInput() // 隐藏键盘
            false
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> IntentUtil.intent(this, SettingsActivity::class.java)
            R.id.menu_clear_messages -> {
                DialogUtil(this, getString(R.string.clear)).apply {
                    setMessage(getString(R.string.clear_confirm))
                    setCancelable(true)
                    setPositiveButton(android.R.string.ok
                    ) { _: DialogInterface?, _: Int ->
                        threadPool.cancel()
                        mChatMessages = mutableListOf()
                        adapter.setMessageList(mChatMessages)
                        RoomHelper.getInstance().chatMessageDao().deleteAll(chat?.title!!)
                    }
                    setNegativeButton(android.R.string.cancel
                    ) { _: DialogInterface?, _: Int ->
                        dismiss()
                    }
                    build()
                    show()
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendMessage() {
        val userInput = binding.inputContainer.etMessage.text.toString()
        if (!TextUtils.isEmpty(userInput)) {
            showLoading()
            threadPool.executor.execute {
                val userInputMessage = generateMessage(userInput, Role.USER.name)
                RoomHelper.getInstance().chatMessageDao().insert(userInputMessage)
                runOnUiThread {
                    addMessage(userInputMessage)
                    binding.inputContainer.etMessage.setText("")
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
                            messageEnd(countDownLatch)
                        }
                    }

                    override fun onClosed(eventSource: EventSource) {
                        super.onClosed(eventSource)
                        messageEnd(countDownLatch)
                    }

                    @SneakyThrows
                    override fun onFailure(
                        eventSource: EventSource,
                        t: Throwable?,
                        response: Response?
                    ) {
                        super.onFailure(eventSource, t, response)
                        messageEnd(countDownLatch)
                    }
                }

                val chatCompletion =
                    ChatCompletion.builder()
                        .messages(DataHelper.getMessageContext(chat!!.prompt!!, chat!!.title!!))
                        .model(PreferencesManager.getOpenAIModel())
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

    private fun messageEnd(countDownLatch: CountDownLatch) {
        countDownLatch.countDown()
        runOnUiThread {
            hideLoading()
        }
    }

    private fun showLoading() {
        binding.inputContainer.progressCircular.visibility = View.VISIBLE
        binding.inputContainer.btnSend.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.inputContainer.progressCircular.visibility = View.GONE
        binding.inputContainer.btnSend.visibility = View.VISIBLE
    }

    private fun addMessage(chatMessage: ChatMessage) {
        mChatMessages?.apply {
            if (!contains(chatMessage)) {
                add(chatMessage)
                binding.rvChatList.adapter?.notifyItemInserted(size - 1)
                binding.rvChatList.scrollToPosition(size - 1)
            } else {
                notifyLastMessage()
            }
        }
    }

    private fun notifyLastMessage() {
        runOnUiThread {
            mChatMessages?.apply {
                if (isNotEmpty()) {
                    binding.rvChatList.adapter?.notifyItemChanged(size - 1)
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