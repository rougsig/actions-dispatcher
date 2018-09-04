package com.github.rougsig.actionsdispatcher.processor

import com.squareup.kotlinpoet.FileSpec
import javax.lang.model.element.TypeElement

internal interface Generator {
  fun generateFile(): FileSpec
}