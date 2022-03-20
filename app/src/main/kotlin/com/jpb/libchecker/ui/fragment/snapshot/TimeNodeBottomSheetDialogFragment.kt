package com.jpb.libchecker.ui.fragment.snapshot

import android.view.ViewGroup
import com.jpb.libchecker.base.BaseBottomSheetViewDialogFragment
import com.jpb.libchecker.database.entity.TimeStampItem
import com.jpb.libchecker.utils.extensions.dp
import com.jpb.libchecker.utils.extensions.putArguments
import com.jpb.libchecker.view.app.BottomSheetHeaderView
import com.jpb.libchecker.view.detail.EmptyListView
import com.jpb.libchecker.view.snapshot.TimeNodeBottomSheetView

const val EXTRA_TOP_APPS = "EXTRA_TOP_APPS"

class TimeNodeBottomSheetDialogFragment :
  BaseBottomSheetViewDialogFragment<TimeNodeBottomSheetView>() {

  private var itemClickAction: ((position: Int) -> Unit)? = null
  private var customTitle: String? = null

  override fun initRootView(): TimeNodeBottomSheetView = TimeNodeBottomSheetView(requireContext())

  override fun getHeaderView(): BottomSheetHeaderView = root.getHeaderView()

  override fun init() {
    customTitle?.let { getHeaderView().title.text = it }
    itemClickAction?.let {
      root.adapter.apply {
        setOnItemClickListener { _, _, position ->
          it(position)
        }
        setEmptyView(
          EmptyListView(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT
            ).also {
              it.bottomMargin = 16.dp
            }
          }
        )
      }
    }

    arguments?.getParcelableArrayList<TimeStampItem>(EXTRA_TOP_APPS)?.let { topApps ->
      root.adapter.setList(topApps)
    }
  }

  fun setTitle(title: String) {
    customTitle = title
  }

  fun setOnItemClickListener(action: (position: Int) -> Unit) {
    itemClickAction = action
  }

  companion object {
    fun newInstance(topApps: ArrayList<TimeStampItem>): TimeNodeBottomSheetDialogFragment {
      return TimeNodeBottomSheetDialogFragment().putArguments(
        EXTRA_TOP_APPS to topApps
      )
    }
  }
}
