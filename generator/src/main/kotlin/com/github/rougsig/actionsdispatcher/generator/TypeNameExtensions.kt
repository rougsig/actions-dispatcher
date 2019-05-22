package com.github.rougsig.actionsdispatcher.generator

import com.squareup.kotlinpoet.TypeName

fun TypeName.asNullable() = copy(nullable = true)
