package com.github.rougsig.actionsdispatcher.compiler

import com.squareup.kotlinpoet.TypeName

internal fun TypeName.asNullable() = copy(nullable = true)
