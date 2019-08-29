package com.github.rougsig.actionsdispatcher.compiler

import com.github.rougsig.actionsdispatcher.runtime.BaseActionsReducer
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName

object ActionDispatcherGenerator {

  internal val BASE_ACTION_REDUCER_TYPE = BaseActionsReducer::class.asTypeName()

  fun generate(params: Params): List<FileSpec> {
    return emptyList()
  }

  data class Params(
    val packageName: String,

    val stateType: TypeName,
    val commandType: TypeName,
    val processFunctionPrefix: String,
    val reducerName: String,
    val receiverName: String,

    val actions: List<Action>
  ) {
    data class Action(
      val name: String,
      val type: TypeName,
      val implementationType: ImplementationType
    )

    enum class ImplementationType {
      None,
      Stub,
      Copy
    }
  }
}
