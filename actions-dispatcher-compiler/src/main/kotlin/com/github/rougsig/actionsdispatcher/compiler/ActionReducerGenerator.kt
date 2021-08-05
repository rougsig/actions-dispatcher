package com.github.rougsig.actionsdispatcher.compiler

import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator.BASE_ACTION_REDUCER_TYPE

internal fun generateActionReducer(params: ActionDispatcherGenerator.Params): ActionDispatcherGenerator.File {
  val sb = StringBuilder()

  with(params) {
    sb.appendLine("package $packageName")
    sb.appendLine()
    sb.appendLine("import ${BASE_ACTION_REDUCER_TYPE.canonicalName}")
    sb.appendLine("import ${stateClassName.canonicalName}")
    sb.appendLine("import ${actionClassName.canonicalName}")
    actions.forEach { action -> sb.appendLine("import ${action.className.canonicalName}") }
    sb.appendLine()
    sb.appendLine("internal class $reducerName private constructor(")
    sb.indent().appendLine("private val receiver: $receiverName")
    sb.appendLine(") : BaseActionsReducer<${stateClassName.simpleName}, ${actionClassName.simpleName}> {")
    sb.indent().appendLine("override fun reduce(")
    sb.indent(2).appendLine("previousState: ${stateClassName.simpleName},")
    sb.indent(2).appendLine("action: ${actionClassName.simpleName}")
    sb.indent().appendLine("): Pair<${stateClassName.simpleName}, (() -> ${actionClassName.simpleName}?)?> {")
    sb.indent(2).appendLine("return when (action) {")
    actions.forEach { action ->
      sb.indent(3).append("is ${action.className.simpleName} -> ")
      sb.appendLine("receiver.$processFunctionPrefix${action.className.simpleName}(previousState, action)")
    }
    sb.indent(2).appendLine("}")
    sb.indent().appendLine("}")
    sb.appendLine()
    sb.indent().appendLine("class Builder {")
    sb.indent(2).appendLine("private var receiver: $receiverName? = null")
    sb.appendLine()
    sb.indent(2).appendLine("fun receiver(receiver: $receiverName) = apply {")
    sb.indent(3).appendLine("this.receiver = receiver")
    sb.indent(2).appendLine("}")
    sb.appendLine()
    sb.indent(2).appendLine("fun build(): $reducerName {")
    sb.indent(3).appendLine("checkNotNull(receiver) { \"no target specified, use receiver Builder`s method to set it\" }")
    sb.indent(3).appendLine("return $reducerName(receiver!!)")
    sb.indent(2).appendLine("}")
    sb.indent().appendLine("}")
    sb.appendLine("}")
  }

  return ActionDispatcherGenerator.File(
    params.reducerName,
    params.packageName,
    sb.toString()
  )
}
