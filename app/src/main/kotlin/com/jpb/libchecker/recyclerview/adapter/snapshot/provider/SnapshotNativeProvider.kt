package com.jpb.libchecker.recyclerview.adapter.snapshot.provider

import android.view.ContextThemeWrapper
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import com.jpb.libchecker.R

import com.jpb.libchecker.base.BaseActivity
import com.jpb.libchecker.bean.ADDED
import com.jpb.libchecker.bean.CHANGED
import com.jpb.libchecker.bean.REMOVED
import com.jpb.libchecker.recyclerview.adapter.snapshot.node.SnapshotNativeNode
import com.jpb.libchecker.ui.fragment.detail.LibDetailDialogFragment
import com.jpb.libchecker.utils.LCAppUtils
import com.jpb.libchecker.utils.extensions.toColorStateList
import com.jpb.libchecker.view.snapshot.SnapshotDetailNativeView
import com.absinthe.libraries.utils.utils.AntiShakeUtils
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.launch

const val SNAPSHOT_NATIVE_PROVIDER = 2

class SnapshotNativeProvider(val lifecycleScope: LifecycleCoroutineScope) : BaseNodeProvider() {

  override val itemViewType: Int = SNAPSHOT_NATIVE_PROVIDER
  override val layoutId: Int = 0

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    return BaseViewHolder(
      SnapshotDetailNativeView(ContextThemeWrapper(context, R.style.AppListMaterialCard)).also {
        it.layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT
        )
      }
    )
  }

  override fun convert(helper: BaseViewHolder, item: BaseNode) {
    (helper.itemView as SnapshotDetailNativeView).container.apply {
      val snapshotItem = (item as SnapshotNativeNode).item

      name.text = snapshotItem.title
      libSize.text = snapshotItem.extra

      val colorRes = when (snapshotItem.diffType) {
        ADDED -> R.color.material_green_300
        REMOVED -> R.color.material_red_300
        CHANGED -> R.color.material_yellow_300
        else -> throw IllegalArgumentException("wrong diff type")
      }

      typeIcon.setImageResource(
        when (snapshotItem.diffType) {
          ADDED -> R.drawable.ic_add
          REMOVED -> R.drawable.ic_remove
          CHANGED -> R.drawable.ic_changed
          else -> throw IllegalArgumentException("wrong diff type")
        }
      )

      helper.itemView.backgroundTintList = colorRes.toColorStateList(context)

      lifecycleScope.launch {
        val rule = LCAppUtils.getRuleWithRegex(snapshotItem.name, snapshotItem.itemType)

        setChip(rule, colorRes)
        if (rule != null) {
          setChipOnClickListener {
            if (AntiShakeUtils.isInvalidClick(it)) {
              return@setChipOnClickListener
            }
            val name = item.item.name
            val regexName = LCAppUtils.findRuleRegex(name, item.item.itemType)?.regexName
            LibDetailDialogFragment.newInstance(name, item.item.itemType, regexName)
              .apply {
                show(
                  (this@SnapshotNativeProvider.context as BaseActivity<*>).supportFragmentManager,
                  tag
                )
              }
          }
        } else {
          setChipOnClickListener(null)
        }
      }
    }
  }
}
