package com.jpb.libchecker.recyclerview.adapter.snapshot

import android.view.ViewGroup
import com.jpb.libchecker.R

import com.jpb.libchecker.bean.ADDED
import com.jpb.libchecker.bean.CHANGED
import com.jpb.libchecker.bean.MOVED
import com.jpb.libchecker.bean.REMOVED
import com.jpb.libchecker.recyclerview.adapter.snapshot.node.SnapshotDetailCountNode
import com.jpb.libchecker.utils.extensions.toColorStateList
import com.jpb.libchecker.view.snapshot.SnapshotDetailCountView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * <pre>
 * author : Absinthe
 * time : 2020/09/27
 * </pre>
 */
class SnapshotDetailCountAdapter : BaseQuickAdapter<SnapshotDetailCountNode, BaseViewHolder>(0) {

  override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    return BaseViewHolder(SnapshotDetailCountView(context))
  }

  override fun convert(holder: BaseViewHolder, item: SnapshotDetailCountNode) {
    val colorRes = when (item.status) {
      ADDED -> R.color.material_green_200
      REMOVED -> R.color.material_red_200
      CHANGED -> R.color.material_yellow_200
      MOVED -> R.color.material_blue_200
      else -> throw IllegalArgumentException("wrong diff type")
    }

    (holder.itemView as SnapshotDetailCountView).apply {
      text = item.count.toString()
      backgroundTintList = colorRes.toColorStateList(context)
    }
  }
}
