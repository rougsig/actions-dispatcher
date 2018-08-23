package ru.rougsig.actionsdispatcher.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class ActionDispatcher(
  val state: KClass<*>,
  val prefix: String = "process",
  val receiverName: String = "ActionReceiver",
  val receiver: KClass<*> = Nothing::class
)