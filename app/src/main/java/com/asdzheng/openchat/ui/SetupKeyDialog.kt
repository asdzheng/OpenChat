package com.asdzheng.openchat.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import cn.iwgang.simplifyspan.SimplifySpanBuild
import cn.iwgang.simplifyspan.customspan.CustomClickableSpan
import cn.iwgang.simplifyspan.unit.SpecialClickableUnit
import cn.iwgang.simplifyspan.unit.SpecialTextUnit
import com.asdzheng.openchat.R
import com.asdzheng.openchat.net.OpenClient
import com.asdzheng.openchat.util.PreferencesManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * @author zhengjb
 * @date on 2023/3/21
 */
class SetupKeyDialog : BottomSheetDialogFragment() {
    private lateinit var mEditText: EditText
    private lateinit var mOkButton: Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the dialog layout
        val view = layoutInflater.inflate(R.layout.dialog_init, null)

        // Get references to the views in the dialog
        val introTextView = view.findViewById<TextView>(R.id.tv_key_step_introduction)
        mEditText = view.findViewById(R.id.et_input)
        mOkButton = view.findViewById(R.id.dialog_ok)

        // Set the text for the views
        introTextView.text = SimplifySpanBuild(getString(R.string.step_one))
            .append(
                SpecialTextUnit(getString(R.string.api_keys)).setClickableUnit(SpecialClickableUnit(
                    introTextView
                ) { tv: TextView?, clickableSpan: CustomClickableSpan? ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://platform.openai.com/account/api-keys")
                        )
                    )
                }).setTextColor(
                    Color.BLUE
                )
            )
            .append("\n\n")
            .append(getString(R.string.step_two))
            .append("\n\n")
            .append(getString(R.string.step_three))
            .build()
        mEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                mOkButton.isEnabled = s.toString().isNotEmpty()
            }
        })


        // Set up the OK button click listener
        mOkButton.setOnClickListener { v: View? ->
            val key = mEditText.text.toString()
            PreferencesManager.setOpenAIAPIKey(key)
            OpenClient.build(key)
            dismissAllowingStateLoss()
        }
        return view
    }
}