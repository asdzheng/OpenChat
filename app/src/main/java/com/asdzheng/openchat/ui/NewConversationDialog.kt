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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogNewConversationBinding.inflate(inflater)

        binding.dialogOk.setOnClickListener {
            var title = binding.etTitle.text.toString()
            var prompt = binding.etPrompt.text.toString()
            if (title.isEmpty()) {
                title = getString(R.string.casual_chat)
            }
            if (prompt.isEmpty()) {
                prompt = getString(R.string.casual_prompt)
            }
            val newChat = Chat().also {
                it.title = title
                it.prompt = prompt
                it.type = DataHelper.ChatType.SUGGESTION.name
            }
            (activity as PromptListActivity).createNewConversation(newChat)
            dismissAllowingStateLoss()
        }

        return binding.root
    }

}