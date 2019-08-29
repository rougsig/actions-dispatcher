package com.github.rougsig.actionsdispatcher.compiler

import com.github.rougsig.actionsdispatcher.runtime.BaseActionsReducer
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.asTypeName

object ActionDispatcherGenerator {

  internal val BASE_ACTION_REDUCER_TYPE = BaseActionsReducer::class.asTypeName()

  fun generate(params: Params): List<FileSpec> {
    return listOf(
      generateActionReceiver(params),
      generateActionReducer(params)
    )
  }

  data class Params(
    val packageName: String,

    val stateClassName: ClassName,
    val processFunctionPrefix: String,
    val reducerName: String,
    val receiverName: String,

    val actionClassName: ClassName,
    val actions: List<Action>
  ) {
    data class Action(
      val className: ClassName,
      val implementationType: ImplementationType
    )

    sealed class ImplementationType {
      object None : ImplementationType()
      object Stub : ImplementationType()
      data class Copy(val fieldName: String) : ImplementationType()
    }
  }
}
