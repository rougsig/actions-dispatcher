package ru.rougsig.actionsdispatcher.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class ActionDispatcher(
  val state: KClass<*>,
  val prefix: String = "",
  val receiverName: String = "",
  val dispatcherName: String = "",
  val receiver: KClass<*> = Nothing::class
)