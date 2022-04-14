package com.jpb.libchecker.ui.fragment

import android.view.Menu
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewbinding.ViewBinding
import com.jpb.libchecker.base.BaseActivity
import com.jpb.libchecker.base.BaseFragment
import com.jpb.libchecker.viewmodel.HomeViewModel
import rikka.material.app.AppBar
import rikka.widget.borderview.BorderViewDelegate

abstract class BaseListControllerFragment<T : ViewBinding> : BaseFragment<T>(), IListController {

  protected var borderDelegate: BorderViewDelegate? = null
  protected val homeViewModel: HomeViewModel by activityViewModels()
  protected var isListReady = false
  protected var allowRefreshing = true
  protected var menu: Menu? = null

  override fun onVisibilityChanged(visible: Boolean) {
    super.onVisibilityChanged(visible)
    if (visible) {
      if (this != homeViewModel.controller) {
        homeViewModel.controller = this
      }
      activity?.invalidateOptionsMenu()
    }
  }

  override fun onResume() {
    super.onResume()
    scheduleAppbarRaisingStatus()
  }

  override fun onDetach() {
    super.onDetach()
    if (this == homeViewModel.controller) {
      homeViewModel.controller = null
    }
  }

  override fun getAppBar(): AppBar? = (activity as? BaseActivity<*>)?.appBar

  override fun getBorderViewDelegate(): BorderViewDelegate? = borderDelegate

  override fun scheduleAppbarRaisingStatus() {
    getAppBar()?.setRaised(!(getBorderViewDelegate()?.isShowingTopBorder ?: true))
  }

  override fun isAllowRefreshing(): Boolean = allowRefreshing

  protected fun isListCanScroll(listSize: Int): Boolean {
    if (context == null) {
      return false
    }
    return true
}}
