package com.github.rougsig.actionsdispatcher.compiler

import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator.Params.ImplementationType.*

internal fun generateActionReceiver(params: ActionDispatcherGenerator.Params): ActionDispatcherGenerator.File {
  val sb = StringBuilder()

  with(params) {
    sb.appendLine("package $packageName")
    sb.appendLine()
    sb.appendLine("import ${stateClassName.canonicalName}")
    sb.appendLine("import ${actionClassName.canonicalName}")
    actions.forEach { action -> sb.appendLine("import ${action.className.canonicalName}") }
    sb.appendLine()
    sb.appendLine("internal interface $receiverName {")
    actions.forEachIndexed { index, action ->
      sb.indent().appendLine("fun $processFunctionPrefix${action.className.simpleName}(")
      sb.indent(2).appendLine("previousState: ${stateClassName.simpleName},")
      sb.indent(2).appendLine("action: ${action.className.simpleName}")
      sb.indent().append("): Pair<${stateClassName.simpleName}, (() -> ${actionClassName.simpleName}?)?>")
      when (val impl = action.implementationType) {
        is None -> sb.appendLine()
        is Stub -> {
          sb.appendLine(" {")
          sb.indent(2).appendLine("return previousState to null")
          sb.indent().appendLine("}")
        }
        is Copy -> {
          sb.appendLine(" {")
          sb.indent(2).appendLine("return previousState.copy(")
          sb.indent(3).appendLine("${impl.fieldName} = action.${impl.fieldName}")
          sb.indent(2).appendLine(") to null")
          sb.indent().appendLine("}")
        }
      }
      if (index != actions.lastIndex) sb.appendLine()
    }
    sb.appendLine("}")
  }

  return ActionDispatcherGenerator.File(
    params.receiverName,
    params.packageName,
    sb.toString()
  )
}
