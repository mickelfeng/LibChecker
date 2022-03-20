package com.jpb.libchecker.view.detail

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageButton
import com.jpb.libchecker.R

import com.jpb.libchecker.utils.extensions.dp

class FeatureLabelView(context: Context) : AppCompatImageButton(context) {

  init {
    layoutParams = ViewGroup.MarginLayoutParams(36.dp, 36.dp).also {
      it.marginEnd = 8.dp
    }
    setBackgroundResource(R.drawable.ripple_feature_label)
  }

  fun setFeature(@DrawableRes res: Int, action: () -> Unit) {
    setImageResource(res)
    setOnClickListener {
      action()
    }
  }
}
