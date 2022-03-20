package com.jpb.libchecker.api

import com.jpb.libchecker.api.request.VERSION
import com.jpb.libchecker.constant.Constants
import com.jpb.libchecker.constant.GlobalValues
import com.jpb.libchecker.utils.JsonUtil
import com.jpb.libchecker.utils.extensions.unsafeLazy
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private const val BRANCH_MASTER = "master"
private const val WORKING_BRANCH = BRANCH_MASTER

object ApiManager {

  private const val GITHUB_ROOT_URL =
    "https://raw.githubusercontent.com/jpbandroid/LibChecker-Rules/main/"
  private const val GITEE_ROOT_URL =
    "https://gitee.com/zhaobozhen/LibChecker-Rules/raw/$WORKING_BRANCH/"

  const val GITHUB_NEW_ISSUE_URL =
    "https://github.com/jpbandroid/LibChecker-Rules/issues/new?labels=&template=library-name.md&title=%5BNew+Rule%5D"

  private val root
    get() = when (GlobalValues.repo) {
      Constants.REPO_GITHUB -> GITHUB_ROOT_URL
      Constants.REPO_GITEE -> GITEE_ROOT_URL
      else -> GITHUB_ROOT_URL
    }

  val rulesBundleUrl = "${root}cloud/rules/v$VERSION/rules.db"

  @PublishedApi
  internal val retrofit by unsafeLazy {
    val okHttpClient = OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .build()
    Retrofit.Builder()
      .addConverterFactory(MoshiConverterFactory.create(JsonUtil.moshi))
      .client(okHttpClient)
      .baseUrl(root)
      .build()
  }

  inline fun <reified T : Any> create(): T = retrofit.create(T::class.java)
}
