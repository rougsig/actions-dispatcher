package com.github.rougsig.actionsdispatcher.kapt

import com.github.rougsig.actionsdispatcher.annotations.ActionElement
import com.github.rougsig.actionsdispatcher.annotations.CopyActionElement
import com.github.rougsig.actionsdispatcher.annotations.DefaultActionElement
import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator
import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator.Params.ImplementationType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.StandardLocation
import kotlin.reflect.KClass

@OptIn(KotlinPoetMetadataPreview::class)
class ActionDispatcherProcessor : AbstractProcessor() {
  companion object {
    var IN_TESTS = false

    const val SUPPORTED_OPTION = "kapt.kotlin.generated"
  }

  private val actionElementAnnotationClass = ActionElement::class.java
  private val copyActionElementAnnotationClass = CopyActionElement::class.java
  private val defaultActionElementAnnotationClass = DefaultActionElement::class.java

  private lateinit var types: Types
  private lateinit var elements: Elements
  private lateinit var filer: Filer
  private lateinit var messager: Messager
  private lateinit var options: Map<String,String>

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
    return setOf(SUPPORTED_OPTION)
  }

  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    this.types = processingEnv.typeUtils
    this.elements = processingEnv.elementUtils
    this.filer = processingEnv.filer
    this.messager = processingEnv.messager
    this.options = processingEnv.options
  }

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    roundEnv.getElementsAnnotatedWith(actionElementAnnotationClass).forEach { element ->
      val targetElement = element as TypeElement
      val annotation = targetElement.getAnnotation(actionElementAnnotationClass)
      val params = parseParams(
        targetElement,
        annotation::stateType.asTypeElement(),
        annotation.prefix,
        annotation.reducerName,
        annotation.receiverName
      ) { action, _ ->
        val actionMetadata = action.toImmutableKmClass()
        actionMetadata.sealedSubclasses.map { name ->
          ActionDispatcherGenerator.Params.Action(
            className = name.toClassName(),
            implementationType = ImplementationType.None
          )
        }
      }
      ActionDispatcherGenerator.generate(params).forEach { it.writeToGeneratedDir(element) }
    }

    roundEnv.getElementsAnnotatedWith(copyActionElementAnnotationClass).forEach { element ->
      val targetElement = element as TypeElement
      val annotation = targetElement.getAnnotation(copyActionElementAnnotationClass)
      val params = parseParams(
        targetElement,
        annotation::stateType.asTypeElement(),
        annotation.prefix,
        annotation.reducerName,
        annotation.receiverName
      ) { action, state ->
        val actionMetadata = action.toImmutableKmClass()
        val stateMetadata = state.toImmutableKmClass()

        actionMetadata.sealedSubclasses.map { name ->
          val fieldName = name.split("/").last().removePrefix("Update").let {
            it.take(1).toLowerCase() + it.drop(1)
          }
          val hasFieldInViewState = stateMetadata.constructors.firstOrNull()
            ?.valueParameters?.find { it.name == fieldName } != null

          ActionDispatcherGenerator.Params.Action(
            className = name.toClassName(),
            implementationType = if (hasFieldInViewState) ImplementationType.Copy(fieldName) else ImplementationType.None
          )
        }
      }
      ActionDispatcherGenerator.generate(params).forEach { it.writeToGeneratedDir(element) }
    }

    roundEnv.getElementsAnnotatedWith(defaultActionElementAnnotationClass).forEach { element ->
      val targetElement = element as TypeElement
      val annotation = targetElement.getAnnotation(defaultActionElementAnnotationClass)
      val params = parseParams(
        targetElement,
        annotation::stateType.asTypeElement(),
        annotation.prefix,
        annotation.reducerName,
        annotation.receiverName
      ) { action, _ ->
        val actionMetadata = action.toImmutableKmClass()

        actionMetadata.sealedSubclasses.map { name ->
          ActionDispatcherGenerator.Params.Action(
            className = name.toClassName(),
            implementationType = ImplementationType.Stub
          )
        }
      }
      ActionDispatcherGenerator.generate(params).forEach { it.writeToGeneratedDir(element) }
    }

    return true
  }

  private fun String.toClassName(): ClassName {
    return ClassName.bestGuess(this.replace("/", "."))
  }

  private fun (() -> KClass<*>).asTypeElement(): TypeElement {
    return try {
      (elements.getTypeElement(this().qualifiedName!!).asType() as DeclaredType).asElement() as TypeElement
    } catch (mte: MirroredTypeException) {
      (mte.typeMirror as DeclaredType).asElement() as TypeElement
    }
  }

  private fun parseParams(
    targetElement: TypeElement,
    stateElement: TypeElement,
    prefix: String,
    reducerName: String,
    receiverName: String,
    actionsParser: (action: TypeElement, state: TypeElement) -> List<ActionDispatcherGenerator.Params.Action>
  ): ActionDispatcherGenerator.Params {
    val actionClassName = targetElement.asClassName()
    val packageName = actionClassName.packageName
    val actions = actionsParser(targetElement, stateElement)

    return ActionDispatcherGenerator.Params(
      packageName,
      stateElement.asClassName(),
      prefix,
      reducerName,
      receiverName,
      actionClassName,
      actions
    )
  }

  private fun ActionDispatcherGenerator.File.writeToGeneratedDir(element: TypeElement) {
    if (IN_TESTS) {
      val generatedDir = options[SUPPORTED_OPTION]?.let(::File)
      val outputDirPath = "$generatedDir/${packageName.replace(".", "/")}"
      val outputDir = File(outputDirPath).also { it.mkdirs() }

      val file = File(outputDir, "$name.kt")
      file.writeText(text)
    } else {
      filer
        .createResource(
          StandardLocation.SOURCE_OUTPUT,
          packageName,
          "$name.kt",
          element
        )
        .openWriter()
        .apply {
          write(text)
          flush()
        }
    }
  }
}
