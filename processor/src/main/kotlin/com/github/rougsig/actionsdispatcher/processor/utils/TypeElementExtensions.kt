package com.github.rougsig.actionsdispatcher.processor.utils

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.PackageElement

val Element.enclosedMethods: List<ExecutableElement>
  get() {
    return enclosedElements.filter { it.kind == ElementKind.METHOD }.map { it as ExecutableElement }
  }

val Element.enclosingPackage: PackageElement
  get() {
    var enclosing: Element? = this
    while (enclosing != null && enclosing.kind != ElementKind.PACKAGE) {
      enclosing = enclosing.enclosingElement
    }
    return (enclosing as? PackageElement) ?: throw IllegalStateException("no package element found")
  }