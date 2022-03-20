package com.jpb.libchecker.bean

data class TrackListItem(
  val label: String,
  val packageName: String,
  var switchState: Boolean = false
)
