package com.github.rougsig.actionsdispatcher.compiler

import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator.BASE_ACTION_REDUCER_TYPE
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal fun generateActionReducer(params: ActionDispatcherGenerator.Params): FileSpec {
  val returnType = Pair::class
    .asTypeName()
    .parameterizedBy(
      params.stateClassName,
      LambdaTypeName.get(returnType = params.actionClassName.copy(nullable = true))
    )

  val receiverClassName = ClassName.bestGuess("${params.packageName}.${params.receiverName}")

  val baseActionReducerType = BASE_ACTION_REDUCER_TYPE
    .parameterizedBy(params.stateClassName, params.actionClassName)

  return FileSpec
    .builder(params.packageName, params.reducerName)
    .addType(TypeSpec
      .classBuilder(params.reducerName)
      .addModifiers(KModifier.INTERNAL)
      .addSuperinterface(baseActionReducerType)
      .primaryConstructor(FunSpec
        .constructorBuilder()
        .addModifiers(KModifier.PRIVATE)
        .addParameter(RECEIVER_PARAMETER_NAME, receiverClassName)
        .build())
      .addType(TypeSpec
        .classBuilder(BUILDER_CLASS_NAME)
        .addProperty(PropertySpec
          .builder(RECEIVER_PARAMETER_NAME, receiverClassName.copy(nullable = true), KModifier.PRIVATE)
          .mutable()
          .initializer("null")
          .build())
        .addFunction(FunSpec
          .builder(RECEIVER_PARAMETER_NAME)
          .addParameter(RECEIVER_PARAMETER_NAME, receiverClassName)
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
          .addStatement("return %N(this.%N!!)", params.reducerName, RECEIVER_PARAMETER_NAME)
          .returns(ClassName.bestGuess(params.reducerName))
          .build())
        .build())
      .addProperty(PropertySpec
        .builder(RECEIVER_PARAMETER_NAME, receiverClassName, KModifier.PRIVATE)
        .initializer(RECEIVER_PARAMETER_NAME)
        .build())
      .addFunction(FunSpec
        .builder("reduce")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter(PREVIOUS_STATE_PARAMETER_NAME, params.stateClassName)
        .addParameter(ACTION_PARAMETER_NAME, params.actionClassName)
        .beginControlFlow("return when (action)")
        .apply {
          params.actions.forEach { action ->
            addCode("is ${action.className.simpleName} -> ")
            addCode("%N.%N(%N, %N)\n",
              RECEIVER_PARAMETER_NAME,
              "${params.processFunctionPrefix}${action.className.simpleName}",
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
