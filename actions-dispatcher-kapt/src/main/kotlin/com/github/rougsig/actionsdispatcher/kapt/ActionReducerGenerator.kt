package com.github.rougsig.actionsdispatcher.kapt

import com.github.rougsig.actionsdispatcher.kapt.utils.asNullable
import com.squareup.kotlinpoet.*

internal class ActionReducerGenerator(
  private val receiverType: ReceiverType,
  private val actionType: ActionType,
  private val actionElementProvider: ActionElementProvider
) : Generator {
  override fun generateFile(): FileSpec {
    val receiverTypeName = ClassName.bestGuess(receiverType.canonicalName)

    return FileSpec.builder(receiverType.packageName, actionElementProvider.reducerName)
      .addType(TypeSpec.classBuilder(actionElementProvider.reducerName)
        .apply { if (actionType.isInternal) addModifiers(KModifier.INTERNAL) }
        .addSuperinterface(actionElementProvider.baseActionsReducerName)
        .primaryConstructor(FunSpec.constructorBuilder()
          .addModifiers(KModifier.PRIVATE)
          .addParameter(RECEIVER_PARAMETER_NAME, receiverTypeName)
          .build())
        .addType(TypeSpec.classBuilder(BUILDER_CLASS_NAME)
          .addProperty(PropertySpec.builder(BUILDER_PARAMETER_RECEIVER, receiverTypeName.asNullable(), KModifier.PRIVATE)
            .mutable()
            .initializer("null")
            .build())
          .addFunction(FunSpec.builder(BUILDER_RECEIVER_SETTER)
            .addParameter(BUILDER_PARAMETER_RECEIVER, receiverTypeName)
            .addStatement("this.%N = %N", BUILDER_PARAMETER_RECEIVER, BUILDER_PARAMETER_RECEIVER)
            .addStatement("return this")
            .returns(ClassName.bestGuess(BUILDER_CLASS_NAME))
            .build())
          .addFunction(FunSpec.builder("build")
            .beginControlFlow("if (this.%N == null)", BUILDER_PARAMETER_RECEIVER)
            .addStatement(
              "throw %T(%S)",
              IllegalStateException::class.java,
              "no target specified, use $BUILDER_RECEIVER_SETTER Builder's method to set it")
            .endControlFlow()
            .addStatement("return %N(this.%N!!)", actionElementProvider.reducerName, BUILDER_PARAMETER_RECEIVER)
            .returns(ClassName.bestGuess(actionElementProvider.reducerName))
            .build())
          .build())
        .addProperty(PropertySpec.builder(RECEIVER_PARAMETER_NAME, receiverTypeName, KModifier.PRIVATE)
          .initializer(RECEIVER_PARAMETER_NAME)
          .build())
        .addFunction(FunSpec.builder("reduce")
          .addModifiers(KModifier.OVERRIDE)
          .addParameter(PREVIOUS_STATE_PARAMETER_NAME, actionElementProvider.stateName)
          .addParameter(ACTION_PARAMETER_NAME, actionType.name)
          .beginControlFlow("return when (action)")
          .apply {
            receiverType.processFunctions.forEach { (actionName, funName) ->
              addCode("is ${actionName.simpleName} -> ")
              addCode("$RECEIVER_PARAMETER_NAME.$funName($PREVIOUS_STATE_PARAMETER_NAME, $ACTION_PARAMETER_NAME)\n")
            }
          }
          .endControlFlow()
          .returns(actionElementProvider.nullableStateCommandPairName)
          .build())
        .build())
      .build()
  }
}

private const val PREVIOUS_STATE_PARAMETER_NAME = "previousState"
private const val ACTION_PARAMETER_NAME = "action"
private const val RECEIVER_PARAMETER_NAME = "receiver"

private const val BUILDER_CLASS_NAME = "Builder"
private const val BUILDER_RECEIVER_SETTER = "receiver"
private const val BUILDER_PARAMETER_RECEIVER = "receiver"
