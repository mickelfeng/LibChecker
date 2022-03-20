package com.jpb.libchecker.bean

import android.os.Parcelable
import com.jpb.libchecker.annotation.LibType
import kotlinx.parcelize.Parcelize

@Parcelize
data class LibReference(
  val libName: String,
  val chip: LibChip?,
  val referredList: List<String>,
  @LibType val type: Int
) : Parcelable
