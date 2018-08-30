package ru.rougsig.actionsdispatcher.utils

import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

val VariableElement.superclass: TypeMirror
  get() = ((this.asType() as DeclaredType).asElement() as TypeElement).superclass

fun Element.asTypeElement() = (this.asType() as DeclaredType).asElement() as TypeElement