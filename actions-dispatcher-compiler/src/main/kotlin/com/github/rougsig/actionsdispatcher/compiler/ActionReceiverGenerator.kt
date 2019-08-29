package com.github.rougsig.actionsdispatcher.compiler

import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator.Params.ImplementationType.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal fun generateActionReceiver(params: ActionDispatcherGenerator.Params): FileSpec {
  val returnType = Pair::class
    .asTypeName()
    .parameterizedBy(
      params.stateClassName,
      LambdaTypeName.get(returnType = params.actionClassName.copy(nullable = true))
    )

  return FileSpec
    .builder(params.packageName, params.receiverName)
    .addType(TypeSpec
      .interfaceBuilder(params.receiverName)
      .addModifiers(KModifier.INTERNAL)
      .addFunctions(params.actions.map { action ->
        FunSpec
          .builder("${params.processFunctionPrefix}${action.className.simpleName}")
          .addParameter(PREVIOUS_STATE_PARAMETER_NAME, params.stateClassName)
          .addParameter(ACTION_PARAMETER_NAME, action.className)
          .returns(returnType)
          .apply {
            when (val implementationType = action.implementationType) {
              is None -> {
                addModifiers(KModifier.ABSTRACT)
              }
              is Stub -> {
                addCode("return $PREVIOUS_STATE_PARAMETER_NAME to null")
              }
              is Copy -> {
                addCode("""
                  |return $PREVIOUS_STATE_PARAMETER_NAME.copy(
                  |    ${implementationType.fieldName} = action.${implementationType.fieldName}
                  |) to null
                """.trimMargin())
              }
            }
          }
          .build()
      })
      .build())
    .build()
}

private const val PREVIOUS_STATE_PARAMETER_NAME = "previousState"
private const val ACTION_PARAMETER_NAME = "action"
