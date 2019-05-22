package com.github.rougsig.actionsdispatcher.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName

class ActionReducerGenerator : Generator<ActionReducerGenerator.Params> {

  override fun generate(params: Params): FileSpec {
    return FileSpec
      .builder(params.packageName, params.reducerName)
      .build()
  }

  data class Params(
    val packageName: String,
    val reducerName: String,
    val receiverType: TypeName,
    val baseActionType: TypeName,
    val stateType: TypeName,
    val isInternal: Boolean,
    val actions: List<Action>
  ) {
    data class Action(
      val name: String,
      val type: TypeName
    )
  }

}

private const val PREVIOUS_STATE_PARAMETER_NAME = "previousState"
private const val ACTION_PARAMETER_NAME = "action"
private const val RECEIVER_PARAMETER_NAME = "receiver"

private const val BUILDER_CLASS_NAME = "Builder"
private const val BUILDER_RECEIVER_SETTER = "receiver"
private const val BUILDER_PARAMETER_RECEIVER = "receiver"
