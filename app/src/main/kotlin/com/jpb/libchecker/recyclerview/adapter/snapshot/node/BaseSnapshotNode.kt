package com.jpb.libchecker.recyclerview.adapter.snapshot.node

import com.jpb.libchecker.bean.SnapshotDetailItem
import com.chad.library.adapter.base.entity.node.BaseNode

open class BaseSnapshotNode(val item: SnapshotDetailItem) : BaseNode() {

  override val childNode: MutableList<BaseNode>? = null
}
