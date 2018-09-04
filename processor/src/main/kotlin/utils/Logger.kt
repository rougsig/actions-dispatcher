package com.github.rougsig.actionsdispatcher.processor.utils

import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

internal class Logger(private val messager: Messager) {
  fun note(message: String, element: Element) {
    messager.printMessage(Diagnostic.Kind.NOTE, message, element)
  }

  fun warning(message: String, element: Element) {
    messager.printMessage(Diagnostic.Kind.WARNING, message, element)
  }

  fun error(message: String, element: Element) {
    messager.printMessage(Diagnostic.Kind.ERROR, message, element)
  }
}