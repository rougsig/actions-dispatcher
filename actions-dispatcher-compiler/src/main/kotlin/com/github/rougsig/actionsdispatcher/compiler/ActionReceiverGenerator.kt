package com.github.rougsig.actionsdispatcher.compiler

import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator.Params.ImplementationType.*

internal fun generateActionReceiver(params: ActionDispatcherGenerator.Params): ActionDispatcherGenerator.File {
  val sb = StringBuilder()

  with(params) {
    sb.appendln("package $packageName")
    sb.appendln()
    sb.appendln("import ${stateClassName.canonicalName}")
    sb.appendln("import ${actionClassName.canonicalName}")
    actions.forEach { action -> sb.appendln("import ${action.className.canonicalName}") }
    sb.appendln()
    sb.appendln("internal interface $receiverName {")
    actions.forEachIndexed { index, action ->
      sb.indent().appendln("fun $processFunctionPrefix${action.className.simpleName}(")
      sb.indent(2).appendln("previousState: ${stateClassName.simpleName},")
      sb.indent(2).appendln("action: ${action.className.simpleName}")
      sb.indent().append("): Pair<${stateClassName.simpleName}, (() -> ${actionClassName.simpleName}?)?>")
      when (val impl = action.implementationType) {
        is None -> sb.appendln()
        is Stub -> {
          sb.appendln(" {")
          sb.indent(2).appendln("return previousState to null")
          sb.indent().appendln("}")
        }
        is Copy -> {
          sb.appendln(" {")
          sb.indent(2).appendln("return previousState.copy(")
          sb.indent(3).appendln("${impl.fieldName} = action.${impl.fieldName}")
          sb.indent(2).appendln(") to null")
          sb.indent().appendln("}")
        }
      }
      if (index != actions.lastIndex) sb.appendln()
    }
    sb.appendln("}")
  }

  return ActionDispatcherGenerator.File(
    params.receiverName,
    params.packageName,
    sb.toString()
  )
}
