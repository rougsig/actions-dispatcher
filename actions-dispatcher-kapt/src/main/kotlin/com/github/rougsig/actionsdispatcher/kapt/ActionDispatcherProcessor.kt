package com.github.rougsig.actionsdispatcher.kapt

import com.github.rougsig.actionsdispatcher.annotations.ActionElement
import com.github.rougsig.actionsdispatcher.annotations.CopyActionElement
import com.github.rougsig.actionsdispatcher.annotations.DefaultActionElement
import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator
import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator.Params.ImplementationType
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.asClassName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException
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
        annotation::stateType.asClassName(),
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
        annotation::stateType.asClassName(),
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
        annotation::stateType.asClassName(),
        annotation.prefix,
        annotation.reducerName,
        annotation.receiverName,
        ImplementationType.Stub
      )
      ActionDispatcherGenerator.generate(params).forEach { it.writeToGeneratedDir() }
    }

    return true
  }

  private fun NameResolver.getClassName(index: Int): ClassName {
    return ClassName.bestGuess(getQualifiedClassName(index).replace("/", "."))
  }

  private fun (() -> KClass<*>).asClassName(): ClassName {
    return try {
      this().asClassName()
    } catch (mte: MirroredTypeException) {
      ((mte.typeMirror as DeclaredType).asElement() as TypeElement).asClassName()
    }
  }

  private fun parseParams(
    targetElement: TypeElement,
    stateClassName: ClassName,
    prefix: String,
    reducerName: String,
    receiverName: String,
    implementationType: ImplementationType
  ): ActionDispatcherGenerator.Params {
    val className = targetElement.asClassName()
    val packageName = className.packageName

    val metadata = targetElement.kotlinMetadata as KotlinClassMetadata
    val proto = metadata.data.classProto
    val nameResolver = metadata.data.nameResolver
    val actions = proto.sealedSubclassFqNameList.map { nameRes ->
      val actionClassName = nameResolver.getClassName(nameRes)

      ActionDispatcherGenerator.Params.Action(
        className = actionClassName,
        implementationType = implementationType
      )
    }

    return ActionDispatcherGenerator.Params(
      packageName,
      stateClassName,
      prefix,
      reducerName,
      receiverName,
      actions
    )
  }

  private fun FileSpec.writeToGeneratedDir() {
    val outputDirPath = "$generatedDir/${packageName.replace(".", "/")}"
    val outputDir = File(outputDirPath).also { it.mkdirs() }

    val file = File(outputDir, "$name.kt")
    file.writeText(toString())
  }
}

private const val KAPT_GENERATED_OPTION = "kapt.kotlin.generated"
