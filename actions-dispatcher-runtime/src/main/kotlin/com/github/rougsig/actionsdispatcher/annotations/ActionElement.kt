package com.github.rougsig.actionsdispatcher.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class ActionElement(
  val state: KClass<*>,
  val prefix: String = "",
  val reducerName: String = "",
  val receiverName: String = "",
  val isDefaultGenerationEnabled: Boolean = false
) {
  companion object {
    const val DEFAULT_PREFIX = "process"
    const val DEFAULT_REDUCER_NAME = "ActionsReducer"
    const val DEFAULT_RECEIVER_NAME = "ActionReceiver"
  }
}
