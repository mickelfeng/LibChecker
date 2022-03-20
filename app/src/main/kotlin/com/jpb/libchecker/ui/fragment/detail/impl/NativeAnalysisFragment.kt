package com.jpb.libchecker.ui.fragment.detail.impl

import android.view.ViewGroup
import com.jpb.libchecker.R

import com.jpb.libchecker.annotation.NATIVE
import com.jpb.libchecker.bean.LibStringItemChip
import com.jpb.libchecker.databinding.FragmentLibNativeBinding
import com.jpb.libchecker.recyclerview.diff.LibStringDiffUtil
import com.jpb.libchecker.ui.detail.EXTRA_PACKAGE_NAME
import com.jpb.libchecker.ui.fragment.BaseDetailFragment
import com.jpb.libchecker.ui.fragment.EXTRA_TYPE
import com.jpb.libchecker.ui.fragment.detail.LocatedCount
import com.jpb.libchecker.utils.extensions.putArguments
import com.jpb.libchecker.utils.showToast
import com.jpb.libchecker.view.detail.NativeLibExtractTipView
import rikka.core.util.ClipboardUtils

class NativeAnalysisFragment : BaseDetailFragment<FragmentLibNativeBinding>() {

  override fun getRecyclerView() = binding.list
  override val needShowLibDetailDialog = true

  override fun init() {
    binding.apply {
      list.apply {
        adapter = this@NativeAnalysisFragment.adapter
      }
    }

    viewModel.nativeLibItems.observe(viewLifecycleOwner) {
      if (it.isEmpty()) {
        emptyView.text.text = getString(R.string.empty_list)
      } else {
        if (viewModel.queriedText?.isNotEmpty() == true) {
          filterList(viewModel.queriedText!!)
        } else {
          adapter.setDiffNewData(it.toMutableList(), afterListReadyTask)
        }

        if (viewModel.extractNativeLibs == false) {
          context?.let { ctx ->
            adapter.setHeaderView(
              NativeLibExtractTipView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.WRAP_CONTENT
                )
              }
            )
          }
        }
      }

      if (!isListReady) {
        viewModel.itemsCountLiveData.value = LocatedCount(locate = type, count = it.size)
        viewModel.itemsCountList[type] = it.size
        isListReady = true
      }
    }

    adapter.apply {
      animationEnable = true
      setOnItemLongClickListener { _, _, position ->
        ClipboardUtils.put(context, getItem(position).item.name)
        context.showToast(R.string.toast_copied_to_clipboard)
        true
      }
      setDiffCallback(LibStringDiffUtil())
      setEmptyView(emptyView)
    }
    viewModel.initSoAnalysisData(packageName)
  }

  override fun getFilterList(text: String): List<LibStringItemChip>? {
    return viewModel.nativeLibItems.value?.filter { it.item.name.contains(text, true) }
  }

  companion object {
    fun newInstance(packageName: String): NativeAnalysisFragment {
      return NativeAnalysisFragment().putArguments(
        EXTRA_PACKAGE_NAME to packageName,
        EXTRA_TYPE to NATIVE
      )
    }
  }
}
