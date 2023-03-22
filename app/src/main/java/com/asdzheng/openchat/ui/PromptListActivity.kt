package com.asdzheng.openchat.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.asdzheng.openchat.R
import com.asdzheng.openchat.databinding.ActivityPromptListBinding
import com.asdzheng.openchat.db.RoomHelper
import com.asdzheng.openchat.db.model.Chat
import com.asdzheng.openchat.db.model.ChatGroup
import com.asdzheng.openchat.util.DataHelper
import com.asdzheng.openchat.util.PreferencesManager
import com.bluewhaleyt.component.snackbar.SnackbarUtil
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.softinput.hideSoftInput
import kotlinx.coroutines.newSingleThreadContext

class PromptListActivity : BaseActivity() {

    private lateinit var binding: ActivityPromptListBinding
    private var mChats: MutableList<Chat>? = null
    private val threadPool = newSingleThreadContext("chat")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromptListBinding.inflate(layoutInflater)
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
        }
    }

    private fun initialize() {
        // 键盘弹出平滑动画
        loadChatsData()
        setupChatList()
        binding.inputContainer.btnSend.setOnClickListener {
        }
        binding.inputContainer.layoutMessageInputContainer.setBackgroundResource(R.drawable.bg_input_container)
    }

    private fun loadChatsData() {
        threadPool.executor.execute {
            mChats = RoomHelper.getInstance().chatDao().queryAll()
        }
    }

    private fun setupChatList() {
        binding.rvPrompts.linear()
//            .divider {
//                setDrawable(R.drawable.divider)
//            }
            .setup {
                addType<ChatGroup>(R.layout.item_group_space)
                addType<Chat>(R.layout.item_chat)
                onBind {
//                    Log.i("main", "id = " + getModel<ChatMessage>().id + " | role " + getModel<ChatMessage>().role)

                    when (itemViewType) {
                        R.layout.item_chat -> {
                            val chat = getModel<Chat>()
                            findView<TextView>(R.id.tv_title).text = chat.title
                            findView<TextView>(R.id.tv_prompt).text = chat.prompt
                            findView<ImageView>(R.id.iv_chat).setImageResource(chat.icon)
                        }
                    }

                }
            }.models = getChatGroupData()
        binding.rvPrompts.setOnTouchListener { v, _ ->
            v.clearFocus() // 清除文字选中状态
            hideSoftInput() // 隐藏键盘
            false
        }
    }

    private fun getChatGroupData(): List<ChatGroup?> {
        val groupDefault = ChatGroup(true, 0, DataHelper.generateDefaultChat(this))
        val groupSuggestions = ChatGroup(true, 1, DataHelper.generateSuggestionsChat(this))
        return listOf(groupDefault, groupSuggestions)
    }

}