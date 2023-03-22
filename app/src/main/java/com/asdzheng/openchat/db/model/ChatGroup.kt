package com.asdzheng.openchat.db.model

import com.drake.brv.item.ItemExpand


/**
 * @author zhengjb
 * @date on 2023/3/22
 */
class ChatGroup(
    override var itemExpand: Boolean,
    override var itemGroupPosition: Int,
    override var itemSublist: List<Any?>?
) : ItemExpand