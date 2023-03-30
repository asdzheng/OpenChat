package com.asdzheng.openchat.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.asdzheng.openchat.R
import com.asdzheng.openchat.databinding.ActivityPromptListBinding
import com.asdzheng.openchat.db.RoomHelper
import com.asdzheng.openchat.db.model.Chat
import com.asdzheng.openchat.db.model.ChatGroup
import com.asdzheng.openchat.util.DataHelper
import com.asdzheng.openchat.util.PreferencesManager
import com.bluewhaleyt.common.IntentUtil
import com.bluewhaleyt.component.dialog.DialogUtil
import com.bluewhaleyt.component.snackbar.SnackbarUtil
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.softinput.hideSoftInput
import kotlinx.coroutines.newSingleThreadContext

class PromptListActivity : BaseActivity() {

    private lateinit var binding: ActivityPromptListBinding
    private val threadPool = newSingleThreadContext("chat")
    private lateinit var suggestionChats: MutableList<Chat>
    private var setupKeyDialog: SetupKeyDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromptListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        initialize()
        setupChatGPT()
    }

    override fun onRestart() {
        super.onRestart()
        loadGroupData()
    }

    private fun setupChatGPT() {
        if (PreferencesManager.getOpenAIAPIKey().trim().isEmpty()) {
            SnackbarUtil.makeSnackbar(this, getString(R.string.api_key_missing))
            if (setupKeyDialog == null) {
                setupKeyDialog = SetupKeyDialog()
                setupKeyDialog?.show(supportFragmentManager, "SetupKeyDialogFragment")
            } else {
                if (!setupKeyDialog?.isAdded!!) {
                    setupKeyDialog?.show(supportFragmentManager, "SetupKeyDialogFragment")
                }
            }

        }
    }

    private fun initialize() {
        loadGroupData()
        // 键盘弹出平滑动画
        setupChatList()
        binding.inputContainer.btnSend.setOnClickListener {
            val message = binding.inputContainer.etMessage.text
            if (message.isNullOrEmpty().not()) {
                jumpToChatActivity(DataHelper.generateDefaultChat(this), message.toString())
                binding.inputContainer.etMessage.setText("")
            }
        }
        binding.inputContainer.layoutMessageInputContainer.setBackgroundResource(R.drawable.bg_input_container)
    }

    private fun loadGroupData() {
        threadPool.executor.execute {
            val groupData = getChatGroupData()
            runOnUiThread {
                binding.rvPrompts.models = groupData
            }
        }
    }

    private fun setupChatList() {
        binding.rvPrompts
            .setup {
                addType<ChatGroup>(R.layout.item_group_space)
                addType<Chat>(R.layout.item_chat)
                onBind {
//                    Log.i("main", "id = " + getModel<ChatMessage>().id + " | role " + getModel<ChatMessage>().role)
                    when (itemViewType) {
                        R.layout.item_chat -> {
                            val parentPosition = findParentPosition()
                            val groupSize =
                                findParentViewHolder()?.getModelOrNull<ChatGroup>()?.itemSublist?.size
                            val chat = getModel<Chat>()
                            findView<TextView>(R.id.tv_title).text = chat.title
                            findView<TextView>(R.id.tv_prompt).text = chat.prompt
                            findView<ImageView>(R.id.iv_chat).setImageResource(chat.icon)
                            val container = findView<View>(R.id.container_chat)
                            if (groupSize == 1) {
                                container.setBackgroundResource(R.drawable.chat_bg_corner)
                            } else if (modelPosition == findParentPosition() + 1) {
                                container.setBackgroundResource(R.drawable.chat_bg_top_corner)
                            } else if (modelPosition == (parentPosition + (groupSize ?: 0))) {
                                container.setBackgroundResource(R.drawable.chat_bg_bottom_corner)
                            }
                        }
                    }
                }

                onClick(R.id.container_chat) {
                    jumpToChatActivity(getModel(), null)
                }

                onLongClick(R.id.container_chat) {

                    val chat = getModel<Chat>()
                    if(DataHelper.ChatType.DEFAULT.name != chat.type) {
                        val dialog = DialogUtil(this@PromptListActivity)
                        dialog.setTitle(getString(R.string.delete))
                        dialog.setMessage(getString(R.string.delete_chat_confirmation))
                        dialog.setPositiveButton(
                            android.R.string.ok
                        ) { d: DialogInterface?, i: Int ->
                            (binding.rvPrompts.adapter as BindingAdapter)._data?.remove(chat)
                            notifyDataSetChanged()

//                            threadPool.executor.execute {
                                RoomHelper.getInstance().chatDao().delete(chat)
//                            }
                        }
                        dialog.setNegativeButton(android.R.string.cancel, null)
                        dialog.create()
                        dialog.show()
                    }
                }
            }

        binding.rvPrompts.setOnTouchListener { v, _ ->
            v.clearFocus() // 清除文字选中状态
            hideSoftInput() // 隐藏键盘
            false
        }
    }

    private fun jumpToChatActivity(chat: Chat, message: String?) {
        val intent = Intent(this@PromptListActivity, ChatActivity::class.java).apply {
            putExtra("data", chat)
            putExtra("message", message)
        }
        startActivity(intent)
    }

    private fun getChatGroupData(): List<ChatGroup?> {
        val groupDefault = ChatGroup(true, 0, listOf(DataHelper.generateDefaultChat(this)))
        suggestionChats = DataHelper.getSuggestionsChat(this)
        val groupSuggestions = ChatGroup(true, 1, suggestionChats)
        return listOf(groupDefault, groupSuggestions)
    }

    fun createNewConversation(chat: Chat) {
        suggestionChats.add(chat)
        jumpToChatActivity(chat, null)
        threadPool.executor.execute {
            RoomHelper.getInstance().chatDao().insert(chat)
            runOnUiThread {
                binding.rvPrompts.models = getChatGroupData()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_conversation -> {
                NewConversationDialog().show(supportFragmentManager, "NewConversationDialog")
            }
            R.id.menu_edit -> {
                IntentUtil.intent(this, SettingsActivity::class.java)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}