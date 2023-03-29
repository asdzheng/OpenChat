package com.asdzheng.openchat.db.model

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.asdzheng.openchat.R
import com.asdzheng.openchat.util.PreferencesManager
import java.util.*

/**
 * @author zhengjb
 * @date on 2023/3/19
 */
@Entity
class Chat : java.io.Serializable {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var title: String? = null
    var prompt: String? = null
    @DrawableRes
    var icon = R.drawable.baseline_question_answer_black_24
    var type: String? = null
    var model: String? = PreferencesManager.getOpenAIModel()
    var uuid : String = UUID.randomUUID().toString()
}