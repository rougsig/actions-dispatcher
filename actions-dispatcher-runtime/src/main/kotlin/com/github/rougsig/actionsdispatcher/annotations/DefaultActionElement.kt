package com.github.rougsig.actionsdispatcher.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class DefaultActionElement(
  val stateType: KClass<*>,
  val commandTypes: KClass<*>,
  val prefix: String = DEFAULT_PREFIX,
  val reducerName: String = DEFAULT_REDUCER_NAME,
  val receiverName: String = DEFAULT_RECEIVER_NAME
)
