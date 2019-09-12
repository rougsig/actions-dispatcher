package com.github.rougsig.actionsdispatcher.compiler

internal fun StringBuilder.indent(n: Int = 1): StringBuilder {
  append(" ".repeat(n * 2))
  return this
}
