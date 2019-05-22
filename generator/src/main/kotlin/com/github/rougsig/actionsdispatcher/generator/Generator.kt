package com.github.rougsig.actionsdispatcher.generator

import com.squareup.kotlinpoet.FileSpec

internal interface Generator<T : Any> {
  fun generate(params: T): FileSpec
}
