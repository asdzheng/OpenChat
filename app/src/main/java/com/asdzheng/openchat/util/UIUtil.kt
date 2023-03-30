package com.asdzheng.openchat.util

import androidx.appcompat.app.AppCompatActivity
import com.asdzheng.openchat.R
import com.asdzheng.openchat.ui.SetupKeyDialog
import com.bluewhaleyt.component.snackbar.SnackbarUtil


/**
 * @author zhengjb
 * @date on 2023/3/30
 */
object UIUtil {
    fun checkSetupChatGPT(setupKeyDialog: SetupKeyDialog?, context: AppCompatActivity): SetupKeyDialog? {
        if (PreferencesManager.openAIAPIKey?.trim().isNullOrEmpty()) {
            SnackbarUtil.makeSnackbar(context, context.getString(R.string.api_key_missing))
            if (setupKeyDialog == null) {
                val dialog = SetupKeyDialog()
                dialog.show(context.supportFragmentManager, "SetupKeyDialogFragment")
                return dialog
            } else {
                if (!setupKeyDialog.isAdded) {
                    setupKeyDialog.show(context.supportFragmentManager, "SetupKeyDialogFragment")
                }
                return setupKeyDialog
            }
        }
        return null
    }
}