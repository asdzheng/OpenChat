package com.asdzheng.openchat.ui

import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import com.asdzheng.openchat.R
import com.asdzheng.openchat.databinding.ActivityMainBinding
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

    private var mChatMessages: MutableList<ChatMessage>? = null
    private lateinit var chat: Chat
    private lateinit var adapter: MessageAdapter
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
        if (PreferencesManager.getOpenAIAPIKey().trim().isEmpty()) {
            SnackbarUtil.makeSnackbar(this, getString(R.string.api_key_missing))
            val dialog = SetupKeyDialog();
            dialog.show(supportFragmentManager, "SetupKeyDialogFragment");
        } else {
            binding.layoutMessageInputContainer.visibility = View.VISIBLE
            binding.rvChatList.visibility = View.VISIBLE
        }
    }

    private fun initialize() {
        chat = DataHelper.generateDefaultChat(getString(R.string.casual_chat), getString(R.string.casual_chat))
        loadHistoryData()
        setupChatList()
        binding.btnSend.setOnClickListener {
            sendMessage()
        }
        binding.etMessage.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus ) {
                binding.etMessage.postDelayed({scrollToEnd(false)},200)
            }
        }

        val gd = GradientDrawable()
        val dynamic = DynamicColorsUtil(this)
        gd.setColor(dynamic.colorPrimaryContainer)
        gd.alpha = 100
        gd.cornerRadius = 100f
        binding.etMessage.background = gd
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
            mChatMessages = RoomHelper.getInstance().chatMessageDao().queryAll()
            runOnUiThread {
                adapter.setMessageList(mChatMessages)
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                scrollToEnd(false)
                binding.etMessage.requestFocus()
                binding.etMessage.postDelayed(
                    {
                        imm.showSoftInput(binding.etMessage, 0)
                    }
                    , 500
                )
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
//        binding.rvChatList.linear()
//            .divider {
//                setDrawable(R.drawable.divider)
//            }
//            .setup {
//                addType<ChatMessage> {
//                    when(role) {
//                        Role.USER.name -> R.layout.layout_message_user_input
//                        else -> R.layout.layout_message_chatgpt_item
//                    }
//                }
//                onBind {
//                    Log.i("main", "id = " + getModel<ChatMessage>().id + " | role " + getModel<ChatMessage>().role)
//
//                    when(getModel<ChatMessage>().role) {
//                        Role.USER.name -> {
//                            findView<TextView>(R.id.tv_user_input_message).text = getModel<ChatMessage>().content
//                        }
//                        else -> {
//                            findView<MarkedView>(R.id.tv_chat_reply_markdown_message).setMDText(getModel<ChatMessage>().content)
//                        }
//                    }
//
//                }
//        }
        binding.rvChatList.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(SCROLL_STATE_DRAGGING == newState) {
                    val inputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                   inputMethodManager.hideSoftInputFromWindow(
                        binding.etMessage.windowToken,
                        0
                    )
                    binding.etMessage.clearFocus()
                }
            }
        })
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
                        RoomHelper.getInstance().chatMessageDao().deleteAll(chat.title!!)
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
        val userInput = binding.etMessage.text.toString()
        if (!TextUtils.isEmpty(userInput)) {
            showLoading()
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
                        .messages(DataHelper.getMessageContext(chat.prompt!!, chat.title!!))
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
        binding.progressCircular.visibility = View.VISIBLE
        binding.btnSend.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressCircular.visibility = View.GONE
        binding.btnSend.visibility = View.VISIBLE
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