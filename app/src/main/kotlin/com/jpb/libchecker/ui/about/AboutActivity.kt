package com.jpb.libchecker.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.jpb.libchecker.R
import com.jpb.libchecker.BuildConfig

import com.jpb.libchecker.constant.Constants
import com.jpb.libchecker.constant.GlobalValues
import com.jpb.libchecker.constant.URLManager
import com.jpb.libchecker.utils.PackageUtils
import com.jpb.libchecker.utils.showLongToast
import com.absinthe.libraries.me.Absinthe
import com.drakeet.about.AbsAboutActivity
import com.drakeet.about.Card
import com.drakeet.about.Category
import com.drakeet.about.Contributor
import com.drakeet.about.License
import com.jpb.libchecker.ui.oss.licenses.OSSLicense
import com.jpb.libchecker.utils.extensions.getColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

private const val RENGE_CHECKER = "RengeChecker"

class AboutActivity : AbsAboutActivity() {

  private var shouldShowEasterEggCount = 1
  private val configuration by lazy {
    Configuration(resources.configuration).apply {
      setLocale(
        GlobalValues.locale
      )
    }
  }
  private val mediaPlayer by lazy { MediaPlayer() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
  }

  override fun onDestroy() {
    super.onDestroy()
    if (shouldShowEasterEggCount == 20) {
      mediaPlayer.release()
    }
  }

  override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
    icon.load(R.mipmap.ic_launcher_round)
    slogan.setText(R.string.app_name)
    version.text = String.format("Version: %s", BuildConfig.VERSION_NAME)

    val rebornCoroutine = lifecycleScope.launch(Dispatchers.Default) {
      delay(300)
      shouldShowEasterEggCount = if (slogan.text == RENGE_CHECKER) 11 else 1
    }
    icon.setOnClickListener {
      when (shouldShowEasterEggCount) {
        in 0..9 -> {
          rebornCoroutine.cancel()
          shouldShowEasterEggCount++
          rebornCoroutine.start()
        }
        10 -> {
          slogan.text = RENGE_CHECKER
          rebornCoroutine.cancel()
          shouldShowEasterEggCount++
        }
        in 11..19 -> {
          rebornCoroutine.cancel()
          shouldShowEasterEggCount++
          rebornCoroutine.start()
        }
        20 -> {
          rebornCoroutine.cancel()
          shouldShowEasterEggCount++

          val inputStream = assets.open("renge.webp")
          icon.setImageBitmap(BitmapFactory.decodeStream(inputStream))
          slogan.text = "ええ、私もよ。"
          val headerContentLayout =
            findViewById<LinearLayout>(com.drakeet.about.R.id.header_content_layout)
          val drawable = TransitionDrawable(
            arrayOf(
              headerContentLayout.background,
              ColorDrawable(R.color.renge.getColor(this))
            )
          )
          setHeaderBackground(drawable)
          setHeaderContentScrim(ColorDrawable(R.color.renge.getColor(this)))
          window.statusBarColor = R.color.renge.getColor(this)
          drawable.startTransition(250)

          val fd = assets.openFd("renge_no_koe.aac")
          mediaPlayer.also {
            it.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            it.prepare()
            it.start()
          }
          GlobalValues.rengeTheme = !GlobalValues.rengeTheme
        }
      }
    }
  }

  override fun onItemsCreated(items: MutableList<Any>) {

    val hasInstallCoolApk = PackageUtils.isAppInstalled(Constants.PACKAGE_NAME_COOLAPK)

    items.apply {
      add(Category("What's this"))
      add(Card(getStringByConfiguration(R.string.about_info)))

      add(Category("Developers"))
      val developerUrl =
        URLManager.GITHUB_PAGE
      add(Contributor(R.drawable.pic_rabbit, Absinthe.ME, "Original Developer", developerUrl))
      add(Contributor(R.drawable.pic_kali, "Goooler", "Original Developer", "https://github.com/Goooler"))
      add(Contributor(R.drawable.jpb, "jpb", "Developer", "https://github.com/jpbandroid"))
      add(
        Contributor(
          R.drawable.ic_github,
          "Source Code",
          URLManager.GITHUB_REPO_PAGE,
          URLManager.GITHUB_REPO_PAGE
        )
      )

      val list = listOf(
        "https://www.iconfont.cn/",
        "https://lottiefiles.com/22122-fanimation",
        "https://lottiefiles.com/21836-blast-off",
        "https://lottiefiles.com/1309-smiley-stack",
        "https://lottiefiles.com/44836-gray-down-arrow",
        "https://lottiefiles.com/66818-holographic-radar",
        "https://chojugiga.com/2017/09/05/da4choju53_0031/"
      )
      add(Category("Acknowledgements"))
      add(
        Card(
          HtmlCompat.fromHtml(
            getAcknowledgementHtmlString(list),
            HtmlCompat.FROM_HTML_MODE_LEGACY
          )
        )
      )

      add(Category("Declaration"))
      add(Card(getStringByConfiguration(R.string.library_declaration)))
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.about_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
    if (menuItem.itemId == R.id.toolbar_rate) {
      try {
        val intent = Intent(this, OSSLicense::class.java)
        startActivity(intent)
      } catch (e: ActivityNotFoundException) {
        Timber.e(e)
        showLongToast("not working!")
      }
    }
    return super.onOptionsItemSelected(menuItem)
  }

  private fun initView() {
    findViewById<Toolbar>(R.id.toolbar)?.background = null
    val color = getColor(R.color.aboutHeader)
    setHeaderBackground(ColorDrawable(color))
    setHeaderContentScrim(ColorDrawable(color))
  }

  private fun getAcknowledgementHtmlString(list: List<String>): String {
    val sb = StringBuilder()
    val formatItem = "<a href=\"%s\">%s</a><br>"

    sb.append(getStringByConfiguration(R.string.resource_declaration)).append("<br>")
    list.forEach { sb.append(String.format(formatItem, it, it)) }
    return sb.toString()
  }

  private fun getStringByConfiguration(@StringRes res: Int): String =
    createConfigurationContext(configuration).resources.getString(res)
}
