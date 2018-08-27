package ru.rougsig.actionsdispatcher.processor

import asTypeElement
import com.google.auto.service.AutoService
import ru.rougsig.actionsdispatcher.annotations.ActionDispatcher
import ru.rougsig.actionsdispatcher.utils.enclosedMethods
import ru.rougsig.actionsdispatcher.utils.enclosingPackage
import ru.rougsig.actionsdispatcher.utils.getAnnotationMirror
import ru.rougsig.actionsdispatcher.utils.getFieldByName
import superclass
import javax.annotation.processing.*
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

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

    elements.forEach { targetElement ->
      val targetTypeElement = targetElement as TypeElement
      val actionReceiver = getActionReceiverInstance(targetElement)
    }

    return true
  }

  private fun getAllActions(targetElement: TypeElement): List<TypeElement> {
    return targetElement.enclosingPackage.enclosedElements
      .map { it.asTypeElement() }
      .filter { it.superclass.toString() == targetElement.qualifiedName.toString() }
  }

  private fun getActionReceiverInstance(targetElement: TypeElement): TypeElement {
    val funPrefix = getElementPrefix(targetElement)
    val receiverName = getElementReceiverName(targetElement)
    val allActions = getAllActions(targetElement)

    val isCustomReceiverClass = getReceiverClass(targetElement) != null
    return if (isCustomReceiverClass) {
      val receiverElement = getReceiverElement(targetElement)!!
      getValidCustomReceiver(receiverElement, targetElement, allActions)
      receiverElement
    } else {
      // TODO generateActionReceiver(funPrefix, receiverName, allActions)

      targetElement
    }
  }

  private fun getValidCustomReceiver(receiverElement: TypeElement, targetElement: TypeElement, allActions: List<TypeElement>) {
    val receiverActions = getReceiverActions(receiverElement, targetElement)

    val notPresentActions = allActions.foldRight(mutableListOf<String>()) { elem, acc ->
      if (!receiverActions.contains(elem)) acc.add(elem.simpleName.toString())
      acc
    }

    if (notPresentActions.isNotEmpty()) throw IllegalStateException("All actions must be present. Actions to add: ${notPresentActions.joinToString { it }}")
  }

  private fun getReceiverElement(targetElement: TypeElement): TypeElement? {
    val annotation = targetElement.getAnnotationMirror(ActionDispatcher::class)!!
    val receiverValue = annotation.getFieldByName(RECEIVER_FIELD_NAME) ?: return null
    val receiverTypeMirror = receiverValue.value as TypeMirror
    return processingEnv.typeUtils.asElement(receiverTypeMirror) as TypeElement
  }

  private fun getReceiverActions(receiverElement: TypeElement, targetElement: TypeElement): List<TypeElement> {
    return receiverElement.enclosedMethods
      .flatMap { method -> method.parameters.filter { it.superclass.toString() == targetElement.qualifiedName.toString() } }
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

  private fun getReceiverClass(targetElement: TypeElement): KClass<*>? {
    return targetElement.getAnnotation(ActionDispatcher::class.java).receiver
      .takeIf { it.jvmName != Nothing::class.jvmName }
  }
}

private const val RECEIVER_FIELD_NAME = "receiver"

private const val ELEMENT_PREFIX_DEFAULT_VALUE = "process"
private const val RECEIVER_NAME_DEFAULT_VALUE = "ActionReceiver"