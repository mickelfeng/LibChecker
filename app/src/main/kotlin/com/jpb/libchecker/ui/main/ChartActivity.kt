package com.jpb.libchecker.ui.main

import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import com.jpb.libchecker.R

import com.jpb.libchecker.base.BaseActivity
import com.jpb.libchecker.databinding.ActivityChartBinding
import com.jpb.libchecker.ui.fragment.statistics.ChartFragment

class ChartActivity : BaseActivity<ActivityChartBinding>() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setAppBar(binding.appbar, binding.toolbar)
    (binding.root as ViewGroup).bringChildToFront(binding.appbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, ChartFragment())
        .commit()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }
}
