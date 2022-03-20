package com.jpb.libchecker.ui.app

import androidx.viewbinding.ViewBinding
import com.jpb.libchecker.base.BaseActivity
import com.jpb.libchecker.constant.Constants
import com.jpb.libchecker.utils.PackageUtils

abstract class CheckPackageOnResumingActivity<VB : ViewBinding> : BaseActivity<VB>() {
  abstract fun requirePackageName(): String?
  protected var isPackageReady: Boolean = false

  override fun onResume() {
    super.onResume()
    if (isPackageReady) {
      requirePackageName()?.let { pkgName ->
        runCatching {
          if (pkgName.endsWith(Constants.TEMP_PACKAGE)) {
            packageManager.getPackageArchiveInfo(pkgName, 0)
          } else {
            PackageUtils.getPackageInfo(pkgName)
          }
        }.onFailure {
          finish()
        }
      }
    }
  }
}
