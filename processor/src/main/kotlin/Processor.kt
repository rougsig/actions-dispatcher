package com.github.rougsig.actionsdispatcher.processor

import com.github.rougsig.actionsdispatcher.utils.asTypeElement
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.github.rougsig.actionsdispatcher.annotations.ActionDispatcher
import com.github.rougsig.actionsdispatcher.utils.*
import com.github.rougsig.actionsdispatcher.utils.superclass
import java.io.File
import javax.annotation.processing.*
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
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
    val elements = roundEnv.getElementsAnnotatedWith(ActionDispatcher::class.java)

    elements.forEach { elem ->
      val targetElement = elem as TypeElement
      val receiverElement = getReceiverElement(targetElement)
      val funByAction = getFunByAction(targetElement, receiverElement)
      val actionReceiverTypeName = getActionReceiverTypeName(targetElement, receiverElement)

      generateDispatcher(targetElement, funByAction, actionReceiverTypeName)
    }

    return true
  }

  private fun generateDispatcher(
    targetElement: TypeElement,
    funByAction: Map<String, String>,
    receiverName: TypeName
  ) {
    val packageName = targetElement.enclosingPackageName
    val stateTypeName = getStateTypeName(targetElement)
    val actionTypeName = getActionTypeName(targetElement)
    val stateActionPair = getStateNullableActionPairTypeName(targetElement)

    val fileName = getDispatcherName(targetElement)
    val file = FileSpec.builder(packageName, fileName)
      .addType(TypeSpec.classBuilder(fileName)
        .primaryConstructor(FunSpec.constructorBuilder()
          .addModifiers(KModifier.PRIVATE)
          .addParameter(RECEIVER_PARAMETER_NAME, receiverName)
          .build())
        .addType(TypeSpec.classBuilder(BUILDER_CLASS_NAME)
          .addProperty(PropertySpec.varBuilder(BUILDER_PARAMETER_RECEIVER, receiverName.asNullable(), KModifier.PRIVATE)
            .initializer("null")
            .build())
          .addFunction(FunSpec.builder(BUILDER_RECEIVER_SETTER)
            .addParameter(BUILDER_PARAMETER_RECEIVER, receiverName)
            .addStatement("this.%N = %N", BUILDER_PARAMETER_RECEIVER, BUILDER_PARAMETER_RECEIVER)
            .addStatement("return this")
            .returns(ClassName.bestGuess(BUILDER_CLASS_NAME))
            .build())
          .addFunction(FunSpec.builder("build")
            .beginControlFlow("if (this.%N == null)", BUILDER_PARAMETER_RECEIVER)
            .addStatement(
              "throw %T(%S)",
              IllegalStateException::class.java,
              "no target specified, use $BUILDER_RECEIVER_SETTER Builder's method to set it")
            .endControlFlow()
            .addStatement("return %N(this.%N!!)", fileName, BUILDER_PARAMETER_RECEIVER)
            .returns(ClassName.bestGuess(fileName))
            .build())
          .build())
        .addProperty(PropertySpec.builder(RECEIVER_PARAMETER_NAME, receiverName, KModifier.PRIVATE)
          .initializer(RECEIVER_PARAMETER_NAME)
          .build())
        .addFunction(FunSpec.builder("dispatch")
          .addParameter(PREVIOUS_STATE_PARAMETER_NAME, stateTypeName)
          .addParameter(ACTION_PARAMETER_NAME, actionTypeName)
          .beginControlFlow("return when (action)")
          .apply {
            funByAction.forEach { (actionName, funName) ->
              addCode("is ${actionName} -> ")
              addCode("$RECEIVER_PARAMETER_NAME.$funName($PREVIOUS_STATE_PARAMETER_NAME, $ACTION_PARAMETER_NAME)\n")
            }
          }
          .endControlFlow()
          .returns(stateActionPair)
          .build())
        .build())
      .build()

    val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
    file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))

  }

  private fun getAllActions(targetElement: TypeElement): List<TypeElement> {
    return targetElement.enclosingPackage.enclosedElements
      .map { it.asTypeElement() }
      .filter { it.superclass.toString() == targetElement.qualifiedName.toString() }
  }

  private fun getActionReceiverTypeName(targetElement: TypeElement, receiverElement: TypeElement?): TypeName {
    val funPrefix = getElementPrefix(targetElement)
    val receiverName = getElementReceiverName(targetElement)
    val allActions = getAllActions(targetElement)

    return if (receiverElement != null) {
      getValidCustomReceiver(receiverElement, targetElement, allActions)
      receiverElement.asType().asTypeName()
    } else {
      generateActionReceiver(targetElement, receiverName, funPrefix, allActions)
    }
  }

  private fun generateActionReceiver(targetElement: TypeElement, receiverName: String, funPrefix: String, allActions: List<TypeElement>): TypeName {
    val packageName = targetElement.enclosingPackageName
    val stateTypeName = getStateTypeName(targetElement)
    val stateActionPairTypeName = getStateNullableActionPairTypeName(targetElement)

    val file = FileSpec.builder(packageName, receiverName)
      .addType(TypeSpec.interfaceBuilder(receiverName)
        .addFunctions(allActions.map { elem ->
          FunSpec.builder("$funPrefix${elem.simpleName}")
            .addModifiers(KModifier.ABSTRACT)
            .addParameter(PREVIOUS_STATE_PARAMETER_NAME, stateTypeName)
            .addParameter(ACTION_PARAMETER_NAME, elem.asType().asTypeName())
            .returns(stateActionPairTypeName)
            .build()
        })
        .build())
      .build()

    val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
    file.writeTo(File(kaptKotlinGeneratedDir, "$receiverName.kt"))

    return ClassName.bestGuess("$packageName.$receiverName")
  }

  private fun getValidCustomReceiver(receiverElement: TypeElement, targetElement: TypeElement, allActions: List<TypeElement>) {
    val receiverActions = getReceiverActions(targetElement, receiverElement)

    val notPresentActions = allActions.foldRight(mutableListOf<String>()) { elem, acc ->
      if (!receiverActions.contains(elem)) acc.add(elem.simpleName.toString())
      acc
    }

    if (notPresentActions.isNotEmpty()) throw IllegalStateException("All actions must be present. Actions to add: ${notPresentActions.joinToString { it }}")
  }

  private fun getReceiverElement(targetElement: TypeElement): TypeElement? {
    val annotation = targetElement.getAnnotationMirror(ActionDispatcher::class)!!
    val receiverValue = annotation.getFieldByName(ANNOTATION_RECEIVER_FIELD_NAME) ?: return null
    val receiverTypeMirror = receiverValue.value as TypeMirror
    return processingEnv.typeUtils.asElement(receiverTypeMirror) as TypeElement
  }

  private fun getReceiverActions(targetElement: TypeElement, receiverElement: TypeElement): List<TypeElement> {
    val stateTypeName = getStateTypeName(targetElement)
    val actionTypeName = getActionTypeName(targetElement)
    val stateActionPairTypeName = getStateCommandPairTypeName(targetElement)

    return receiverElement.enclosedMethods
      .filter { method -> method.parameters.any { it.superclass.toString() == targetElement.qualifiedName.toString() } }
      .map { method ->
        if (method.returnType.asTypeName().toString().replace(".jvm.functions", "") != stateActionPairTypeName.toString())
          throw IllegalStateException("returns type must be $stateActionPairTypeName. Invalid fun `${method.simpleName}`")
        if (method.parameters[0].asType().asTypeName() != stateTypeName)
          throw IllegalStateException("first parameter must be $stateTypeName. Invalid fun `${method.simpleName}`")
        if (method.parameters[1].superclass.asTypeName() != actionTypeName)
          throw IllegalStateException("second parameter must be $actionTypeName. Invalid fun `${method.simpleName}`")

        method.parameters[1]
      }
      .map { it.asTypeElement() }
  }

  private fun getElementPrefix(targetElement: TypeElement): String {
    return targetElement.getAnnotation(ActionDispatcher::class.java).prefix
      .takeIf { it.isNotBlank() }
      ?: ELEMENT_PREFIX_DEFAULT_VALUE
  }

  private fun getElementReceiverName(targetElement: TypeElement): String {
    return targetElement.getAnnotation(ActionDispatcher::class.java).receiverName
      .takeIf { it.isNotBlank() }
      ?: RECEIVER_NAME_DEFAULT_VALUE
  }

  private fun getDispatcherName(targetElement: TypeElement): String {
    return targetElement.getAnnotation(ActionDispatcher::class.java).dispatcherName
      .takeIf { it.isNotBlank() }
      ?: DISPATCHER_NAME_DEFAULT_VALUE
  }

  private fun getStateElement(targetElement: TypeElement): TypeElement {
    val annotation = targetElement.getAnnotationMirror(ActionDispatcher::class)
      ?: throw IllegalArgumentException("State must by provided")
    val receiverValue = annotation.getFieldByName(ANNOTATION_STATE_FIELD_NAME)
    val receiverTypeMirror = receiverValue!!.value as TypeMirror
    return processingEnv.typeUtils.asElement(receiverTypeMirror) as TypeElement
  }

  private fun getStateTypeName(targetElement: TypeElement): TypeName {
    return getStateElement(targetElement).asType().asTypeName()
  }

  private fun getCommandTypeName(targetElement: TypeElement): TypeName {
    val annotation = targetElement.getAnnotationMirror(ActionDispatcher::class)
    val receiverValue = annotation!!.getFieldByName(ANNOTATION_COMMAND_FIELD_NAME)
    val receiverTypeMirror = (receiverValue?.value as? TypeMirror)
      ?: return ParameterizedTypeName.get(Function0::class.asClassName(), getActionTypeName(targetElement))
    return (processingEnv.typeUtils.asElement(receiverTypeMirror) as TypeElement).asType().asTypeName()
  }

  private fun getActionTypeName(targetElement: TypeElement): TypeName {
    return targetElement.asType().asTypeName()
  }

  private fun getStateNullableActionPairTypeName(targetElement: TypeElement): TypeName {
    val stateTypeName = getStateTypeName(targetElement)
    val commandTypeName = getCommandTypeName(targetElement)
    return ParameterizedTypeName.get(Pair::class.asClassName(), stateTypeName, commandTypeName.asNullable())
  }

  private fun getStateCommandPairTypeName(targetElement: TypeElement): TypeName {
    val stateTypeName = getStateTypeName(targetElement)
    val commandTypeName = getCommandTypeName(targetElement)
    return ParameterizedTypeName.get(Pair::class.asClassName(), stateTypeName, commandTypeName)
  }

  private fun getFunByAction(targetElement: TypeElement, receiverElement: TypeElement?): Map<String, String> {
    return if (receiverElement != null) {
      getCustomReceiverFunByAction(targetElement, receiverElement)
    } else {
      getDefaultReceiverFunByAction(targetElement)
    }
  }

  private fun getCustomReceiverFunByAction(targetElement: TypeElement, receiverElement: TypeElement): Map<String, String> {
    val allActions = getAllActions(targetElement)
    val result = HashMap<String, String>()

    val allMethodsParameters = receiverElement.enclosedMethods
      .map { method -> method.parameters.map { it.asType().asTypeName().toString() } to method.simpleName.toString() }

    allActions.forEach { elem ->
      allMethodsParameters.find { method ->
        method.first.contains(elem.qualifiedName.toString())
      }?.let {
        result[elem.simpleName.toString()] = it.second
      }
    }

    return result
  }

  private fun getDefaultReceiverFunByAction(targetElement: TypeElement): Map<String, String> {
    val allActions = getAllActions(targetElement)
    val funPrefix = getElementPrefix(targetElement)
    val result = HashMap<String, String>()

    allActions.forEach { elem ->
      result[elem.simpleName.toString()] = "$funPrefix${elem.simpleName}"
    }

    return result
  }
}

private const val ANNOTATION_RECEIVER_FIELD_NAME = "receiver"
private const val ANNOTATION_STATE_FIELD_NAME = "state"
private const val ANNOTATION_COMMAND_FIELD_NAME = "command"

private const val ELEMENT_PREFIX_DEFAULT_VALUE = "process"
private const val RECEIVER_NAME_DEFAULT_VALUE = "ActionReceiver"
private const val DISPATCHER_NAME_DEFAULT_VALUE = "ActionsDispatcher"

private const val PREVIOUS_STATE_PARAMETER_NAME = "previousState"
private const val ACTION_PARAMETER_NAME = "action"
private const val RECEIVER_PARAMETER_NAME = "receiver"

private const val BUILDER_CLASS_NAME = "Builder"
private const val BUILDER_RECEIVER_SETTER = "setReceiver"
private const val BUILDER_PARAMETER_RECEIVER = "receiver"