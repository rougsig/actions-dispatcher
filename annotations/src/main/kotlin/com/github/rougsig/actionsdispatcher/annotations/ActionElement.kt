package com.github.rougsig.actionsdispatcher.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class ActionElement(
  val state: KClass<*>,
  val prefix: String = "process",
  val receiverName: String = "ActionReceiver",
  val reducerName: String = "ActionsReducer",
  val receiver: KClass<*> = Nothing::class,
  val command: KClass<*> = Nothing::class,
  val generateDefaultReceiverImplementation: Boolean = false
)
