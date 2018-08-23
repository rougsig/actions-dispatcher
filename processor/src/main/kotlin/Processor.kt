package ru.rougsig.actionsdispatcher.processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import ru.rougsig.actionsdispatcher.annotations.ActionDispatcher
import java.io.File
import java.lang.StringBuilder
import javax.annotation.processing.*
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types

@AutoService(Processor::class)
class Processor : AbstractProcessor() {
  private lateinit var logger: Logger
  private lateinit var typeUtils: Types
  private lateinit var filer: Filer

  companion object {
    const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
  }

  @Synchronized
  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)

    logger = Logger(processingEnv.messager)
    typeUtils = processingEnv.typeUtils
    filer = processingEnv.filer
  }

  override fun getSupportedAnnotationTypes(): Set<String> {
    return setOf(ActionDispatcher::class.java.name)
  }

  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latest()
  }

  override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
    return true
  }
}