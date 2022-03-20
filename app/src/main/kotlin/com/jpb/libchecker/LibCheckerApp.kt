package com.jpb.libchecker

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.window.core.ExperimentalWindowApi
import androidx.window.embedding.SplitController
import com.jpb.libchecker.app.Global
import com.jpb.libchecker.constant.GlobalValues
import com.jpb.libchecker.database.Repositories
import com.jpb.libchecker.utils.LCAppUtils
import com.jpb.libchecker.utils.timber.ReleaseTree
import com.jpb.libchecker.utils.timber.ThreadAwareDebugTree
import com.absinthe.libraries.utils.utils.Utility
import com.google.android.material.color.DynamicColors
import com.jakewharton.processphoenix.ProcessPhoenix
import jonathanfinerty.once.Once
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.material.app.DayNightDelegate
import rikka.material.app.LocaleDelegate
import timber.log.Timber

class LibCheckerApp : Application() {

  override fun onCreate() {
    super.onCreate()

    if (ProcessPhoenix.isPhoenixProcess(this)) {
      return
    }

    if (LCAppUtils.atLeastP()) {
      HiddenApiBypass.addHiddenApiExemptions("")
    }

    app = this

    if (BuildConfig.DEBUG) {
      Timber.plant(ThreadAwareDebugTree())
    } else {
      Timber.plant(ReleaseTree())
    }

    Utility.init(this)
    LocaleDelegate.defaultLocale = GlobalValues.locale
    DayNightDelegate.setApplicationContext(this)
    DayNightDelegate.setDefaultNightMode(LCAppUtils.getNightMode(GlobalValues.darkMode))
    Once.initialise(this)
    Repositories.init(this)
    Repositories.checkRulesDatabase()
    initSplitController()

    if (GlobalValues.md3Theme) {
      DynamicColors.applyToActivitiesIfAvailable(this)
    }
  }

  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
    Global.start()
  }

  @OptIn(ExperimentalWindowApi::class)
  private fun initSplitController() {
    SplitController.initialize(this, R.xml.main_split_config)
  }

  companion object {
    @SuppressLint("StaticFieldLeak")
    lateinit var app: Application
  }
}
