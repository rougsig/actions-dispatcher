package com.github.rougsig.actionsdispatcher.kapt.utils

import com.squareup.kotlinpoet.TypeName

fun TypeName.asNullable() = copy(nullable = true)
