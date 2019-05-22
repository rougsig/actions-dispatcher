package com.github.rougsig.actionsdispatcher.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class ActionReceiverGenerator : Generator<ActionReceiverGenerator.Params> {

  override fun generate(params: Params): FileSpec {
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
            .builder("process${action.name}")
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

  data class Params(
    val packageName: String,
    val receiverName: String,
    val stateType: TypeName,
    val baseActionType: TypeName,
    val isInternal: Boolean,
    val actions: List<Action>,
    val isDefaultGenerationEnabled: Boolean
  ) {
    data class Action(
      val name: String,
      val type: TypeName
    )
  }

}

private const val PREVIOUS_STATE_PARAMETER_NAME = "previousState"
private const val ACTION_PARAMETER_NAME = "action"
