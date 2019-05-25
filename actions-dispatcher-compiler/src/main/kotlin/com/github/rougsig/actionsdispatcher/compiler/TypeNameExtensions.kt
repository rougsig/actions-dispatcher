package com.github.rougsig.actionsdispatcher.compiler

import com.squareup.kotlinpoet.TypeName

fun TypeName.asNullable() = copy(nullable = true)
