package com.github.rougsig.actionsdispatcher.compiler

import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator.BASE_ACTION_REDUCER_TYPE

internal fun generateActionReducer(params: ActionDispatcherGenerator.Params): ActionDispatcherGenerator.File {
  val sb = StringBuilder()

  with(params) {
    sb.appendln("package $packageName")
    sb.appendln()
    sb.appendln("import ${BASE_ACTION_REDUCER_TYPE.canonicalName}")
    sb.appendln("import ${stateClassName.canonicalName}")
    sb.appendln("import ${actionClassName.canonicalName}")
    actions.forEach { action -> sb.appendln("import ${action.className.canonicalName}") }
    sb.appendln()
    sb.appendln("internal class $reducerName private constructor(")
    sb.indent().appendln("private val receiver: $receiverName")
    sb.appendln(") : BaseActionsReducer<${stateClassName.simpleName}, ${actionClassName.simpleName}> {")
    sb.indent().appendln("override fun reduce(")
    sb.indent(2).appendln("previousState: ${stateClassName.simpleName},")
    sb.indent(2).appendln("action: ${actionClassName.simpleName}")
    sb.indent().appendln("): Pair<${stateClassName.simpleName}, (() -> ${actionClassName.simpleName}?)?> {")
    sb.indent(2).appendln("return when (action) {")
    actions.forEach { action ->
      sb.indent(3).append("is ${action.className.simpleName} -> ")
      sb.appendln("receiver.$processFunctionPrefix${action.className.simpleName}(previousState, action)")
    }
    sb.indent(2).appendln("}")
    sb.indent().appendln("}")
    sb.appendln()
    sb.indent().appendln("class Builder {")
    sb.indent(2).appendln("private var receiver: $receiverName? = null")
    sb.appendln()
    sb.indent(2).appendln("fun receiver(receiver: $receiverName) = apply {")
    sb.indent(3).appendln("this.receiver = receiver")
    sb.indent(2).appendln("}")
    sb.appendln()
    sb.indent(2).appendln("fun build(): $reducerName {")
    sb.indent(3).appendln("checkNotNull(receiver) { \"no target specified, use receiver Builder`s method to set it\" }")
    sb.indent(3).appendln("return $reducerName(receiver!!)")
    sb.indent(2).appendln("}")
    sb.indent().appendln("}")
    sb.appendln("}")
  }

  return ActionDispatcherGenerator.File(
    params.reducerName,
    params.packageName,
    sb.toString()
  )
}
