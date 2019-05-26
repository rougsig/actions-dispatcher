package com.github.rougsig.actionsdispatcher.kapt

import com.github.rougsig.actionsdispatcher.annotations.ActionElement
import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.visibility
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException

@AutoService(Processor::class)
class ActionDispatcherProcessor : KotlinAbstractProcessor() {

  companion object {
    const val OPTION_GENERATED = "actionsdispatcher.generated"
    private val POSSIBLE_GENERATED_NAMES = setOf(
      "javax.annotation.processing.Generated",
      "javax.annotation.Generated"
    )
  }

  private val annotation = ActionElement::class.java
  private var generatedType: TypeElement? = null
  private lateinit var logger: Logger

  override fun getSupportedAnnotationTypes() = setOf(annotation.canonicalName)

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun getSupportedOptions() = setOf(OPTION_GENERATED)

  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    logger = Logger(messager)
    generatedType = processingEnv.options[OPTION_GENERATED]?.let {
      if (it !in POSSIBLE_GENERATED_NAMES) {
        throw IllegalArgumentException("Invalid option value for $OPTION_GENERATED. Found $it, " +
          "allowable values are $POSSIBLE_GENERATED_NAMES.")
      }
      processingEnv.elementUtils.getTypeElement(it)
    }
  }

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    for (type in roundEnv.getElementsAnnotatedWith(annotation)) {
      val targetElement = (type.asType() as DeclaredType).asElement() as TypeElement
      val generatorParams = parseParams(logger, targetElement) ?: continue

      val receiverFileSpec = ActionDispatcherGenerator.generateActionReceiver(generatorParams)
      val actionReducerFileSpec = ActionDispatcherGenerator.generateActionReducer(generatorParams)

      writeToDisk(receiverFileSpec)
      writeToDisk(actionReducerFileSpec)
    }
    return true
  }

  private fun parseParams(logger: Logger, targetElement: TypeElement): ActionDispatcherGenerator.Params? {
    val typeMetadata = targetElement.kotlinMetadata
    if (typeMetadata !is KotlinClassMetadata) {
      logger.error("@ActionElement can't be applied to $targetElement: must be kotlin class", targetElement)
      return null
    }
    val proto = typeMetadata.data.classProto

    val actionElementAnnotation = targetElement.getAnnotation(annotation)

    val packageName = targetElement.asClassName().packageName
    val reducerName = actionElementAnnotation.reducerName
    val receiverName = actionElementAnnotation.receiverName
    val receiverType = ClassName.bestGuess(actionElementAnnotation.receiverName)
    val baseActionType = targetElement.asClassName()
    val stateType = try {
      actionElementAnnotation.state
      throw IllegalStateException("actionElementAnnotation.state must throw MirroredTypeException")
    } catch (exception: MirroredTypeException) {
      ClassName.bestGuess(exception.typeMirror.toString())
    }
    val isInternal = proto.visibility!! == ProtoBuf.Visibility.INTERNAL
    val isDefaultGenerationEnabled = actionElementAnnotation.isDefaultGenerationEnabled
    val actions = getActions(targetElement, actionElementAnnotation.prefix)

    return ActionDispatcherGenerator.Params(
      packageName = packageName,
      reducerName = reducerName,
      receiverName = receiverName,
      receiverType = receiverType,
      baseActionType = baseActionType,
      stateType = stateType,
      isInternal = isInternal,
      isDefaultGenerationEnabled = isDefaultGenerationEnabled,
      actions = actions
    )
  }

  private fun getActions(
    actionTypeElement: TypeElement,
    processFunPrefix: String
  ): List<ActionDispatcherGenerator.Params.Action> {
    val actions = mutableListOf<TypeElement>()

    for (element in actionTypeElement.enclosingElement.enclosedElements) {
      val typeMetadata = element.kotlinMetadata

      if (element !is TypeElement || typeMetadata !is KotlinClassMetadata) continue
      if (element.superclass.toString() != actionTypeElement.asType().toString()) continue

      actions.add(element)
    }

    return actions.map { action ->
      val actionClassName = action.asClassName()

      ActionDispatcherGenerator.Params.Action(
        name = actionClassName.simpleName,
        type = actionClassName,
        processFunName = "$processFunPrefix${actionClassName.simpleName}"
      )
    }
  }

  private fun writeToDisk(fileSpec: FileSpec) {
    val fileName = fileSpec.members.filterIsInstance<TypeSpec>().first().name!!
    val outputDir = generatedDir ?: mavenGeneratedDir(fileName)
    fileSpec.writeTo(outputDir)
  }

  private fun mavenGeneratedDir(fileName: String): File {
    // Hack since the maven plugin doesn`t supply `kapt.kotlin.generated` option
    // Bug filed at https://youtrack.jetbrains.com/issue/KT-22783
    val file = filer.createSourceFile(fileName).toUri().let(::File)
    return file.parentFile.also { file.delete() }
  }
}
