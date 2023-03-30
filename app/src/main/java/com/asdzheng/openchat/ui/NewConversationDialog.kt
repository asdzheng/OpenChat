package com.asdzheng.openchat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asdzheng.openchat.R
import com.asdzheng.openchat.databinding.DialogNewConversationBinding
import com.asdzheng.openchat.db.model.Chat
import com.asdzheng.openchat.util.DataHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * @author zhengjb
 * @date on 2023/3/21
 */
internal class NewConversationDialog : BottomSheetDialogFragment() {
    private lateinit var binding: DialogNewConversationBinding

    companion object {
        fun newInstance(chat: Chat): NewConversationDialog {
            val args = Bundle()
            args.putSerializable("data", chat)
            val fragment = NewConversationDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogNewConversationBinding.inflate(inflater)
        val chat = arguments?.getSerializable("data")
        if(chat != null) {
            binding.tvTitle.text = getString(R.string.edit_conversation)
            (chat as Chat).apply {
                binding.etTitle.setText(title)
                binding.etPrompt.setText(prompt)

            }
        }

        binding.dialogOk.setOnClickListener {
            var title = binding.etTitle.text.toString()
            var prompt = binding.etPrompt.text.toString()
            if (title.isEmpty()) {
                title = getString(R.string.casual_chat)
            }
            if (prompt.isEmpty()) {
                prompt = getString(R.string.casual_prompt)
            }

            if (activity is PromptListActivity) {
                (activity as PromptListActivity).createNewConversation(Chat().also {
                    it.title = title
                    it.prompt = prompt
                    it.type = DataHelper.ChatType.USER.name
                })
            } else if (activity is ChatActivity && chat != null) {
                (activity as ChatActivity).editChatComplete( (chat as Chat).also {
                    it.title = title
                    it.prompt = prompt
                })
            }
            dismissAllowingStateLoss()
        }

        return binding.root
    }

}