package com.github.rougsig.actionsdispatcher.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal fun buildActionReducer(params: ActionDispatcherGenerator.Params): FileSpec {
  val returnType = Pair::class
    .asTypeName()
    .parameterizedBy(
      params.stateType,
      Function0::class
        .asTypeName()
        .parameterizedBy(params.baseActionType.asNullable())
    )

  return FileSpec
    .builder(params.packageName, params.reducerName)
    .addType(TypeSpec
      .classBuilder(params.reducerName)
      .apply { if (params.isInternal) addModifiers(KModifier.INTERNAL) }
      .addSuperinterface(BASE_ACTION_REDUCER_TYPE)
      .primaryConstructor(FunSpec
        .constructorBuilder()
        .addModifiers(KModifier.PRIVATE)
        .addParameter(RECEIVER_PARAMETER_NAME, params.receiverType)
        .build())
      .addType(TypeSpec
        .classBuilder(BUILDER_CLASS_NAME)
        .addProperty(PropertySpec
          .builder(RECEIVER_PARAMETER_NAME, params.receiverType.asNullable(), KModifier.PRIVATE)
          .mutable()
          .initializer("null")
          .build())
        .addFunction(FunSpec
          .builder(RECEIVER_PARAMETER_NAME)
          .addParameter(RECEIVER_PARAMETER_NAME, params.receiverType)
          .addStatement("this.%N = %N", RECEIVER_PARAMETER_NAME, RECEIVER_PARAMETER_NAME)
          .addStatement("return this")
          .returns(ClassName.bestGuess(BUILDER_CLASS_NAME))
          .build())
        .addFunction(FunSpec
          .builder("build")
          .beginControlFlow("if (this.%N == null)", RECEIVER_PARAMETER_NAME)
          .addStatement(
            "throw %T(%S)",
            IllegalStateException::class.java,
            "no target specified, use $RECEIVER_PARAMETER_NAME Builder`s method to set it")
          .endControlFlow()
          .addStatement("return %S(this.%S!!)", params.reducerName, RECEIVER_PARAMETER_NAME)
          .returns(ClassName.bestGuess(params.reducerName))
          .build())
        .build())
      .addProperty(PropertySpec
        .builder(RECEIVER_PARAMETER_NAME, params.receiverType, KModifier.PRIVATE)
        .initializer(RECEIVER_PARAMETER_NAME)
        .build())
      .addFunction(FunSpec
        .builder("reduce")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter(PREVIOUS_STATE_PARAMETER_NAME, params.stateType)
        .addParameter(ACTION_PARAMETER_NAME, params.baseActionType)
        .beginControlFlow("return when (action)")
        .apply {
          params.actions.forEach { action ->
            addCode("is ${action.name} -> ")
            addCode("%T.%S(%S, %S)\n",
              RECEIVER_PARAMETER_NAME,
              action.processFunName,
              PREVIOUS_STATE_PARAMETER_NAME,
              ACTION_PARAMETER_NAME)
          }
        }
        .endControlFlow()
        .returns(returnType)
        .build())
      .build())
    .build()
}

private const val BUILDER_CLASS_NAME = "Builder"
private const val PREVIOUS_STATE_PARAMETER_NAME = "previousState"
private const val ACTION_PARAMETER_NAME = "action"
private const val RECEIVER_PARAMETER_NAME = "receiver"
