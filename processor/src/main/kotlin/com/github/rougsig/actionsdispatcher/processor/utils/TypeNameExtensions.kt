package com.github.rougsig.actionsdispatcher.processor.utils

import com.squareup.kotlinpoet.TypeName

fun TypeName.asNullable() = copy(nullable = true)