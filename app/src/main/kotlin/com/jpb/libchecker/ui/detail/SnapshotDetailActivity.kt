package com.jpb.libchecker.ui.detail

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.core.view.PointerIconCompat.load
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.SimpleItemAnimator
import coil.load
import com.jpb.libchecker.R

import com.jpb.libchecker.annotation.ACTIVITY
import com.jpb.libchecker.annotation.LibType
import com.jpb.libchecker.annotation.METADATA
import com.jpb.libchecker.annotation.NATIVE
import com.jpb.libchecker.annotation.PERMISSION
import com.jpb.libchecker.annotation.PROVIDER
import com.jpb.libchecker.annotation.RECEIVER
import com.jpb.libchecker.annotation.SERVICE
import com.jpb.libchecker.bean.ADDED
import com.jpb.libchecker.bean.CHANGED
import com.jpb.libchecker.bean.MOVED
import com.jpb.libchecker.bean.REMOVED
import com.jpb.libchecker.bean.SnapshotDetailItem
import com.jpb.libchecker.bean.SnapshotDiffItem
import com.jpb.libchecker.database.Repositories
import com.jpb.libchecker.databinding.ActivitySnapshotDetailBinding
import com.jpb.libchecker.recyclerview.VerticalSpacesItemDecoration
import com.jpb.libchecker.recyclerview.adapter.snapshot.ARROW
import com.jpb.libchecker.recyclerview.adapter.snapshot.SnapshotDetailAdapter
import com.jpb.libchecker.recyclerview.adapter.snapshot.node.BaseSnapshotNode
import com.jpb.libchecker.recyclerview.adapter.snapshot.node.SnapshotComponentNode
import com.jpb.libchecker.recyclerview.adapter.snapshot.node.SnapshotNativeNode
import com.jpb.libchecker.recyclerview.adapter.snapshot.node.SnapshotTitleNode
import com.jpb.libchecker.ui.app.CheckPackageOnResumingActivity
import com.jpb.libchecker.utils.LCAppUtils
import com.jpb.libchecker.utils.PackageUtils
import com.jpb.libchecker.utils.Toasty
import com.jpb.libchecker.utils.extensions.addPaddingTop
import com.jpb.libchecker.utils.extensions.dp
import com.jpb.libchecker.utils.extensions.sizeToString
import com.jpb.libchecker.utils.extensions.unsafeLazy
import com.jpb.libchecker.view.detail.AppBarStateChangeListener
import com.jpb.libchecker.view.snapshot.SnapshotDetailDeletedView
import com.jpb.libchecker.view.snapshot.SnapshotDetailNewInstallView
import com.jpb.libchecker.view.snapshot.SnapshotEmptyView
import com.jpb.libchecker.viewmodel.SnapshotViewModel
import com.absinthe.libraries.utils.utils.AntiShakeUtils
import com.chad.library.adapter.base.entity.node.BaseNode
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.launch
import me.zhanghai.android.appiconloader.AppIconLoader
import rikka.core.util.ClipboardUtils

const val EXTRA_ENTITY = "EXTRA_ENTITY"

class SnapshotDetailActivity : CheckPackageOnResumingActivity<ActivitySnapshotDetailBinding>() {

  private lateinit var entity: SnapshotDiffItem

  private val adapter by unsafeLazy { SnapshotDetailAdapter(lifecycleScope) }
  private val viewModel: SnapshotViewModel by viewModels()
  private val _entity by unsafeLazy { intent.getSerializableExtra(EXTRA_ENTITY) as? SnapshotDiffItem }

  override fun requirePackageName() = entity.packageName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (_entity != null) {
      entity = _entity!!
      initView()
      viewModel.computeDiffDetail(this, entity)
    } else {
      finish()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.snapshot_detail_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      finish()
    } else if (item.itemId == R.id.report_generate) {
      generateReport()
    }
    return super.onOptionsItemSelected(item)
  }

  @SuppressLint("SetTextI18n")
  private fun initView() {
    setSupportActionBar(binding.toolbar)
    supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
      setDisplayShowHomeEnabled(true)
      title = null
    }

    binding.apply {
      collapsingToolbar.also {
        it.setOnApplyWindowInsetsListener(null)
        it.title = entity.labelDiff.new ?: entity.labelDiff.old
      }
      headerLayout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
        override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
          collapsingToolbar.isTitleEnabled = state == State.COLLAPSED
        }
      })
      list.apply {
        adapter = this@SnapshotDetailActivity.adapter
        (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        addItemDecoration(VerticalSpacesItemDecoration(4.dp))
      }

      val isNewOrDeleted = entity.deleted || entity.newInstalled

      ivAppIcon.apply {
        val appIconLoader = AppIconLoader(
          resources.getDimensionPixelSize(R.dimen.lib_detail_icon_size),
          false,
          this@SnapshotDetailActivity
        )
        val icon = try {
          appIconLoader.loadIcon(
            PackageUtils.getPackageInfo(
              entity.packageName,
              PackageManager.GET_META_DATA
            ).applicationInfo
          )
        } catch (e: PackageManager.NameNotFoundException) {
          null
        }
        load(icon)
        setOnClickListener {
          lifecycleScope.launch {
            val lcItem = Repositories.lcRepository.getItem(entity.packageName) ?: return@launch
            LCAppUtils.launchDetailPage(this@SnapshotDetailActivity, lcItem)
          }
        }
      }
      tvAppName.text = getDiffString(entity.labelDiff, isNewOrDeleted)
      tvPackageName.text = entity.packageName
      tvVersion.text = getDiffString(
        entity.versionNameDiff,
        entity.versionCodeDiff,
        isNewOrDeleted,
        "%s (%s)"
      )
      tvTargetApi.text = "API ${getDiffString(entity.targetApiDiff, isNewOrDeleted)}"

      if (entity.packageSizeDiff.old > 0L) {
        tvPackageSize.isVisible = true
        val sizeDiff = SnapshotDiffItem.DiffNode(
          entity.packageSizeDiff.old.sizeToString(this@SnapshotDetailActivity),
          entity.packageSizeDiff.new?.sizeToString(this@SnapshotDetailActivity)
        )
        tvPackageSize.text = getDiffString(sizeDiff, isNewOrDeleted)
      } else {
        tvPackageSize.isVisible = false
      }
    }

    viewModel.snapshotDetailItems.observe(this) { details ->
      val titleList = mutableListOf<SnapshotTitleNode>()

      getNodeList(details.filter { it.itemType == NATIVE }).apply {
        if (isNotEmpty()) {
          titleList.add(SnapshotTitleNode(this, NATIVE))
        }
      }
      getNodeList(details.filter { it.itemType == SERVICE }).apply {
        if (isNotEmpty()) {
          titleList.add(SnapshotTitleNode(this, SERVICE))
        }
      }
      getNodeList(details.filter { it.itemType == ACTIVITY }).apply {
        if (isNotEmpty()) {
          titleList.add(SnapshotTitleNode(this, ACTIVITY))
        }
      }
      getNodeList(details.filter { it.itemType == RECEIVER }).apply {
        if (isNotEmpty()) {
          titleList.add(SnapshotTitleNode(this, RECEIVER))
        }
      }
      getNodeList(details.filter { it.itemType == PROVIDER }).apply {
        if (isNotEmpty()) {
          titleList.add(SnapshotTitleNode(this, PROVIDER))
        }
      }
      getNodeList(details.filter { it.itemType == PERMISSION }).apply {
        if (isNotEmpty()) {
          titleList.add(SnapshotTitleNode(this, PERMISSION))
        }
      }
      getNodeList(details.filter { it.itemType == METADATA }).apply {
        if (isNotEmpty()) {
          titleList.add(SnapshotTitleNode(this, METADATA))
        }
      }

      if (titleList.isNotEmpty()) {
        adapter.setList(titleList)
      }
    }

    adapter.setEmptyView(
      when {
        entity.newInstalled -> SnapshotDetailNewInstallView(this)
        entity.deleted -> SnapshotDetailDeletedView(this)
        else -> SnapshotEmptyView(this).apply {
          layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
          ).also {
            it.gravity = Gravity.CENTER_HORIZONTAL
          }
          addPaddingTop(96.dp)
        }
      }
    )
    adapter.setOnItemClickListener { _, view, position ->
      if (adapter.data[position] is SnapshotTitleNode) {
        adapter.expandOrCollapse(position)
        return@setOnItemClickListener
      }
      if (AntiShakeUtils.isInvalidClick(view)) {
        return@setOnItemClickListener
      }

      val item = (adapter.data[position] as BaseSnapshotNode).item
      if (item.diffType == REMOVED) {
        return@setOnItemClickListener
      }

      lifecycleScope.launch {
        val lcItem = Repositories.lcRepository.getItem(entity.packageName) ?: return@launch
        LCAppUtils.launchDetailPage(
          this@SnapshotDetailActivity,
          item = lcItem,
          refName = item.name,
          refType = item.itemType
        )
      }
    }
  }

  private fun getNodeList(list: List<SnapshotDetailItem>): MutableList<BaseNode> {
    val returnList = mutableListOf<BaseNode>()

    if (list.isEmpty()) return returnList

    when (list[0].itemType) {
      NATIVE, METADATA -> list.forEach { returnList.add(SnapshotNativeNode(it)) }
      else -> list.forEach { returnList.add(SnapshotComponentNode(it)) }
    }

    return returnList
  }

  private fun <T> getDiffString(
    diff: SnapshotDiffItem.DiffNode<T>,
    isNewOrDeleted: Boolean = false,
    format: String = "%s"
  ): String {
    return if (diff.old != diff.new && !isNewOrDeleted) {
      "${format.format(diff.old)} $ARROW ${format.format(diff.new)}"
    } else {
      format.format(diff.old)
    }
  }

  private fun getDiffString(
    diff1: SnapshotDiffItem.DiffNode<*>,
    diff2: SnapshotDiffItem.DiffNode<*>,
    isNewOrDeleted: Boolean = false,
    format: String = "%s"
  ): String {
    return if ((diff1.old != diff1.new || diff2.old != diff2.new) && !isNewOrDeleted) {
      "${format.format(diff1.old, diff2.old)} $ARROW ${format.format(diff1.new, diff2.new)}"
    } else {
      format.format(diff1.old, diff2.old)
    }
  }

  private fun generateReport() {
    val sb = StringBuilder()
    sb.append(binding.tvAppName.text).appendLine()
      .append(binding.tvPackageName.text).appendLine()
      .append(binding.tvVersion.text).appendLine()
      .append(binding.tvTargetApi.text).appendLine()

    if (binding.tvPackageSize.isVisible) {
      sb.append(binding.tvPackageSize.text).appendLine()
    }

    sb.appendLine()

    adapter.data.forEach {
      when (it) {
        is SnapshotTitleNode -> {
          sb.append("[${getComponentName(it.type)}]").appendLine()
        }
        is SnapshotComponentNode -> {
          sb.append(getDiffTypeLabel(it.item.diffType))
            .append(" ")
            .append(it.item.title)
            .appendLine()
        }
        is SnapshotNativeNode -> {
          sb.append(getDiffTypeLabel(it.item.diffType))
            .append(" ")
            .append(it.item.title)
            .appendLine()
            .append("\t")
            .append(it.item.extra)
            .appendLine()
        }
      }
    }
    ClipboardUtils.put(this, sb.toString())
    Toasty.showShort(this, R.string.toast_copied_to_clipboard)
  }

  private fun getComponentName(@LibType type: Int): String {
    val titleRes = when (type) {
      NATIVE -> R.string.ref_category_native
      SERVICE -> R.string.ref_category_service
      ACTIVITY -> R.string.ref_category_activity
      RECEIVER -> R.string.ref_category_br
      PROVIDER -> R.string.ref_category_cp
      PERMISSION -> R.string.ref_category_perm
      METADATA -> R.string.ref_category_metadata
      else -> android.R.string.untitled
    }
    return getString(titleRes)
  }

  private fun getDiffTypeLabel(diffType: Int): String {
    return when (diffType) {
      ADDED -> "🟢+"
      REMOVED -> "🔴-"
      CHANGED -> "🟡~"
      MOVED -> "🔵<->"
      else -> throw IllegalArgumentException("wrong diff type")
    }
  }
}
