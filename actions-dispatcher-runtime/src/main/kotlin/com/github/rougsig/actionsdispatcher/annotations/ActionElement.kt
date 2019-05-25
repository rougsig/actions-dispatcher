package com.github.rougsig.actionsdispatcher.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class ActionElement(
  val state: KClass<*>,
  val prefix: String = "process",
  val reducerName: String = "ActionsReducer",
  val receiverName: String = "ActionReceiver",
  val isDefaultGenerationEnabled: Boolean = false
)
