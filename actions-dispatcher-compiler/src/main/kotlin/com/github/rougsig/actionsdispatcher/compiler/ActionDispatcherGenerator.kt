package com.github.rougsig.actionsdispatcher.compiler

import com.github.rougsig.actionsdispatcher.parser.ActionElementModelParser
import com.github.rougsig.actionsdispatcher.runtime.BaseActionsReducer
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import java.io.File

object ActionDispatcherGenerator {

  internal val BASE_ACTION_REDUCER_TYPE = BaseActionsReducer::class.asTypeName()

  fun process(source: String, outputDir: File): List<String> {
    val files = process(source)
    files.forEach { it.writeTo(outputDir) }
    return files.map { "${outputDir.absolutePath}/${it.packageName.replace(".", "/")}/${it.name}" }
  }

  internal fun process(source: String): List<FileSpec> {
    val params = ActionElementModelParser.parse(source)?.mapToParams() ?: return emptyList()

    return listOf(
      generateActionReceiver(params),
      generateActionReducer(params)
    )
  }

  internal fun generateActionReceiver(params: Params): FileSpec = buildActionReceiver(params)
  internal fun generateActionReducer(params: Params): FileSpec = buildActionReducer(params)

  internal data class Params(
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
      val type: TypeName,
      val processFunName: String
    )
  }
}
