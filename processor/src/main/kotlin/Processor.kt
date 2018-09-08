package com.github.rougsig.actionsdispatcher.processor

import com.github.rougsig.actionsdispatcher.annotations.ActionElement
import com.github.rougsig.actionsdispatcher.processor.utils.Logger
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.TypeSpec
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class Processor : KotlinAbstractProcessor() {

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
      val actionType = ActionType.get(logger, elementUtils, typeUtils, type) ?: continue
      val actionElement = type.getAnnotation(annotation)
      val actionElementProvider = ActionElementProvider.get(actionElement, actionType, typeUtils)
      val receiverType = ReceiverType.get(logger, actionType, actionElementProvider) ?: continue
      if (receiverType is AutoGeneratedReceiverType) {
        val receiverGenerator = ActionReceiverGenerator(receiverType, actionType, actionElementProvider)
        receiverGenerator.generateAndWrite()
      }
      ActionReducerGenerator(receiverType, actionType, actionElementProvider).generateAndWrite()
    }
    return true
  }

  private fun Generator.generateAndWrite() {
    val fileSpec = generateFile()
    val adapterName = fileSpec.members.filterIsInstance<TypeSpec>().first().name!!
    val outputDir = generatedDir ?: mavenGeneratedDir(adapterName)
    fileSpec.writeTo(outputDir)
  }

  private fun mavenGeneratedDir(adapterName: String): File {
    // Hack since the maven plugin doesn't supply `kapt.kotlin.generated` option
    // Bug filed at https://youtrack.jetbrains.com/issue/KT-22783
    val file = filer.createSourceFile(adapterName).toUri().let(::File)
    return file.parentFile.also { file.delete() }
  }
}