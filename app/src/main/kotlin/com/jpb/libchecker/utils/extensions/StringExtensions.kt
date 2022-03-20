package com.jpb.libchecker.utils.extensions

import com.jpb.libchecker.constant.Constants
import java.io.File

fun String.isTempApk(): Boolean {
  return endsWith("${File.separator}${Constants.TEMP_PACKAGE}")
}
