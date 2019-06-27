package com.github.rougsig.actionsdispatcher.compiler

import com.github.rougsig.actionsdispatcher.parser.ActionElementModelParser
import com.squareup.kotlinpoet.ClassName

internal fun ActionElementModelParser.Model.mapToParams(): ActionDispatcherGenerator.Params {
  return ActionDispatcherGenerator.Params(
    packageName = this.packageName,
    reducerName = this.reducerName,
    receiverName = this.receiverName,
    receiverType = ClassName.bestGuess("${this.packageName}.${this.receiverName}"),
    baseActionType = ClassName.bestGuess("${this.packageName}.${this.baseClassName}"),
    stateType = ClassName.bestGuess(this.stateClassName),
    isInternal = this.isInternal,
    isDefaultGenerationEnabled = this.isDefaultGenerationEnabled,
    actions = this.actions.map { action ->
      ActionDispatcherGenerator.Params.Action(
        type = ClassName.bestGuess("${this.packageName}.$action"),
        processFunName = "${this.prefix}$action"
      )
    }
  )
}
