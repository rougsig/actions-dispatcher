package com.github.rougsig.actionsdispatcher.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal fun buildActionReceiver(params: ActionDispatcherGenerator.Params): FileSpec {
  val returnType = Pair::class
    .asTypeName()
    .parameterizedBy(
      params.stateType,
      Function0::class
        .asTypeName()
        .parameterizedBy(params.baseActionType.asNullable())
    )

  return FileSpec
    .builder(params.packageName, params.receiverName)
    .addType(TypeSpec
      .interfaceBuilder(params.receiverName)
      .apply { if (params.isInternal) addModifiers(KModifier.INTERNAL) }
      .addFunctions(params.actions.map { action ->
        FunSpec
          .builder(action.processFunName)
          .addParameter(PREVIOUS_STATE_PARAMETER_NAME, params.stateType)
          .addParameter(ACTION_PARAMETER_NAME, action.type)
          .returns(returnType)
          .apply {
            if (params.isDefaultGenerationEnabled) addCode("return $PREVIOUS_STATE_PARAMETER_NAME to null")
            else addModifiers(KModifier.ABSTRACT)
          }
          .build()
      })
      .build())
    .build()
}

private const val PREVIOUS_STATE_PARAMETER_NAME = "previousState"
private const val ACTION_PARAMETER_NAME = "action"
