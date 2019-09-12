package com.github.rougsig.actionsdispatcher.kapt

import com.github.rougsig.actionsdispatcher.annotations.ActionElement
import com.github.rougsig.actionsdispatcher.annotations.CopyActionElement
import com.github.rougsig.actionsdispatcher.annotations.DefaultActionElement
import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator
import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator.Params.ImplementationType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.kaptGeneratedOption
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException
import javax.tools.StandardLocation
import kotlin.reflect.KClass

class ActionDispatcherProcessor : KotlinAbstractProcessor() {
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
    return setOf(kaptGeneratedOption)
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
        val metadata = action.kotlinMetadata as KotlinClassMetadata
        val proto = metadata.data.classProto
        val nameResolver = metadata.data.nameResolver

        proto.sealedSubclassFqNameList.map { nameRes ->
          val actionSubclassName = nameResolver.getClassName(nameRes)

          ActionDispatcherGenerator.Params.Action(
            className = actionSubclassName,
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
        val actionMetadata = action.kotlinMetadata as KotlinClassMetadata
        val actionProto = actionMetadata.data.classProto
        val actionNameResolver = actionMetadata.data.nameResolver

        val stateMetadata = state.kotlinMetadata as KotlinClassMetadata
        val stateProto = stateMetadata.data.classProto
        val stateNameResolver = stateMetadata.data.nameResolver

        actionProto.sealedSubclassFqNameList.map { nameRes ->
          val actionSubclassName = actionNameResolver.getClassName(nameRes)
          val fieldName = actionSubclassName.simpleName.removePrefix("Update").let {
            it.take(1).toLowerCase() + it.drop(1)
          }
          val hasFieldInViewState = stateProto.constructorOrBuilderList.firstOrNull()
            ?.valueParameterList?.find { stateNameResolver.getString(it.name) == fieldName } != null

          ActionDispatcherGenerator.Params.Action(
            className = actionSubclassName,
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
        val metadata = action.kotlinMetadata as KotlinClassMetadata
        val proto = metadata.data.classProto
        val nameResolver = metadata.data.nameResolver

        proto.sealedSubclassFqNameList.map { nameRes ->
          val actionSubclassName = nameResolver.getClassName(nameRes)

          ActionDispatcherGenerator.Params.Action(
            className = actionSubclassName,
            implementationType = ImplementationType.Stub
          )
        }
      }
      ActionDispatcherGenerator.generate(params).forEach { it.writeToGeneratedDir(element) }
    }

    return true
  }

  private fun NameResolver.getClassName(index: Int): ClassName {
    return ClassName.bestGuess(getQualifiedClassName(index).replace("/", "."))
  }

  private fun (() -> KClass<*>).asTypeElement(): TypeElement {
    return try {
      (elementUtils.getTypeElement(this().qualifiedName!!).asType() as DeclaredType).asElement() as TypeElement
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
