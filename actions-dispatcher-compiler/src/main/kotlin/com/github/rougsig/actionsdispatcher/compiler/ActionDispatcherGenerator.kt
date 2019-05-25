package com.github.rougsig.actionsdispatcher.compiler

import com.github.rougsig.actionsdispatcher.runtime.BaseActionsReducer
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName

object ActionDispatcherGenerator {

  internal val BASE_ACTION_REDUCER_TYPE = BaseActionsReducer::class.asTypeName()

  fun generateActionReceiver(params: Params): FileSpec = buildActionReceiver(params)
  fun generateActionReducer(params: Params): FileSpec = buildActionReducer(params)

  data class Params(
    val packageName: String,
    val reducerName: String,
    val receiverName: String,
    val receiverType: TypeName,
    val baseActionType: TypeName,
    val stateType: TypeName,
    val isInternal: Boolean,
    val isDefaultGenerationEnabled: Boolean,
    val actions: List<Action>
  ) {
    data class Action(
      val name: String,
      val type: TypeName,
      val processFunName: String
    )
  }

}
