package ru.rougsig.actionsdispatcher.utils

import javax.lang.model.element.*
import kotlin.reflect.KClass

fun TypeElement.getAnnotationMirror(annotationClass: KClass<*>): AnnotationMirror? {
  return annotationMirrors
    .find { it.annotationType.asElement().simpleName.toString() == annotationClass.simpleName.toString() }
}

val Element.enclosedMethods: List<ExecutableElement>
  get() {
    return enclosedElements.filter { it.kind == ElementKind.METHOD }.map { it as ExecutableElement }
  }

val Element.enclosedFields: List<VariableElement>
  get() {
    return enclosedElements.filter(predicate = { it.kind == ElementKind.FIELD }).map { it as VariableElement }
  }

val Element.isNullable: Boolean
  get() {
    return annotationMirrors.any { it.annotationType.asElement().simpleName.endsWith("Nullable") }
  }

val Element.isPublic: Boolean
  get() {
    return modifiers.contains(Modifier.PUBLIC)
  }

val Element.enclosingPackage: PackageElement
  get() {
    var enclosing: Element? = this
    while (enclosing != null && enclosing.kind != ElementKind.PACKAGE) {
      enclosing = enclosing.enclosingElement
    }
    return (enclosing as? PackageElement) ?: throw IllegalStateException("no package element found")
  }

val Element.enclosingPackageName get() = enclosingPackage.qualifiedName.toString()