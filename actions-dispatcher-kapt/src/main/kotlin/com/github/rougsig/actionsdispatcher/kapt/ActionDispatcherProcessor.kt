package com.github.rougsig.actionsdispatcher.kapt

import com.github.rougsig.actionsdispatcher.annotations.ActionElement
import com.github.rougsig.actionsdispatcher.annotations.CopyActionElement
import com.github.rougsig.actionsdispatcher.annotations.DefaultActionElement
import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator
import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator.Params.ImplementationType
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

@AutoService(ActionDispatcherProcessor::class)
class ActionDispatcherProcessor : AbstractProcessor() {
  private val generatedDir: File
    get() = processingEnv.options[KAPT_GENERATED_OPTION].let(::File)

  private val actionElementAnnotationClass = ActionElement::class.java
  private val copyActionElementAnnotationClass = CopyActionElement::class.java
  private val defaultActionElementAnnotationClass = DefaultActionElement::class.java

  override fun getSupportedAnnotationTypes(): Set<String> {
    return setOf(
      actionElementAnnotationClass.canonicalName,
      copyActionElementAnnotationClass.canonicalName,
      defaultActionElementAnnotationClass.canonicalName
    )
  }

  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latest()
  }

  override fun getSupportedOptions(): Set<String> {
    return setOf(KAPT_GENERATED_OPTION)
  }

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    roundEnv.getElementsAnnotatedWith(actionElementAnnotationClass).forEach { element ->
      val targetElement = element as TypeElement
      val annotation = targetElement.getAnnotation(actionElementAnnotationClass)
      val params = parseParams(
        targetElement,
        annotation.stateType,
        annotation.commandType,
        annotation.prefix,
        annotation.reducerName,
        annotation.receiverName,
        ImplementationType.None
      )
      ActionDispatcherGenerator.generate(params).forEach { it.writeToGeneratedDir() }
    }

    roundEnv.getElementsAnnotatedWith(copyActionElementAnnotationClass).forEach { element ->
      val targetElement = element as TypeElement
      val annotation = targetElement.getAnnotation(actionElementAnnotationClass)
      val params = parseParams(
        targetElement,
        annotation.stateType,
        annotation.commandType,
        annotation.prefix,
        annotation.reducerName,
        annotation.receiverName,
        ImplementationType.Copy
      )
      ActionDispatcherGenerator.generate(params).forEach { it.writeToGeneratedDir() }
    }

    roundEnv.getElementsAnnotatedWith(defaultActionElementAnnotationClass).forEach { element ->
      val targetElement = element as TypeElement
      val annotation = targetElement.getAnnotation(actionElementAnnotationClass)
      val params = parseParams(
        targetElement,
        annotation.stateType,
        annotation.commandType,
        annotation.prefix,
        annotation.reducerName,
        annotation.receiverName,
        ImplementationType.Stub
      )
      ActionDispatcherGenerator.generate(params).forEach { it.writeToGeneratedDir() }
    }

    return true
  }

  private fun parseParams(
    targetElement: TypeElement,
    stateType: KClass<*>,
    commandType: KClass<*>,
    prefix: String,
    reducerName: String,
    receiverName: String,
    implementationType: ImplementationType
  ): ActionDispatcherGenerator.Params {
    TODO()
  }

  private fun FileSpec.writeToGeneratedDir() {
    val outputDirPath = "$generatedDir/${packageName.replace(".", "/")}"
    val outputDir = File(outputDirPath).also { it.mkdirs() }

    val file = File(outputDir, "$name.kt")
    file.writeText(toString())
  }
}

private const val KAPT_GENERATED_OPTION = "kapt.kotlin.generated"
