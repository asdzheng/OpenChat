package com.asdzheng.openchat.db.model

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author zhengjb
 * @date on 2023/3/19
 */
@Entity
class Chat {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var title: String? = null
    var prompt: String? = null
    @DrawableRes
    var icon = 0
}