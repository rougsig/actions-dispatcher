package com.github.rougsig.actionsdispatcher.entity

data class ActionElementModel(
  val packageName: String,
  val baseClassName: String,

  val state: String,
  val prefix: String,
  val reducerName: String,
  val receiverName: String,
  val isDefaultGenerationEnabled: Boolean,

  val actions: List<String>
)
