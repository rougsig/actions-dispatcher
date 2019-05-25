package com.github.rougsig.actionsdispatcher.kapt

import com.squareup.kotlinpoet.FileSpec

internal interface Generator {
  fun generateFile(): FileSpec
}
