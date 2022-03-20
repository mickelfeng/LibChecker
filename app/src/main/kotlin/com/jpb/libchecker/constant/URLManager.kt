package com.jpb.libchecker.constant

import com.jpb.libchecker.BuildConfig
import com.absinthe.libraries.me.Absinthe

object URLManager {
  const val MARKET_PAGE = "${Absinthe.MARKET_DETAIL_SCHEME}${BuildConfig.APPLICATION_ID}"
  const val COOLAPK_APP_PAGE = "https://coolapk.com/apk/${BuildConfig.APPLICATION_ID}"

  const val COOLAPK_HOME_PAGE = Absinthe.COOLAPK_HOME_PAGE
  const val GITHUB_PAGE = Absinthe.GITHUB_HOME_PAGE

  const val GITHUB_REPO_PAGE = "https://github.com/jpbandroid/LibChecker"

  const val DOCS_PAGE = "https://github.com/zhaobozhen/LibChecker-Docs"
}
