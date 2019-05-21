package com.github.rougsig.actionsdispatcher.processor

import com.squareup.kotlinpoet.FileSpec

internal interface Generator {
  fun generateFile(): FileSpec
}