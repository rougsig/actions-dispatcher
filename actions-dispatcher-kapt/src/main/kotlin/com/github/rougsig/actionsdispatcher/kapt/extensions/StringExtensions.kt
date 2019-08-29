package com.github.rougsig.actionsdispatcher.kapt.extensions

internal fun String.beginWithUpperCase(): String {
  return when (length) {
    0 -> ""
    1 -> toUpperCase()
    else -> first().toUpperCase() + substring(1)
  }
}
