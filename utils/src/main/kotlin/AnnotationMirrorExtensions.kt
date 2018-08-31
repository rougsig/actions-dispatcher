package com.github.rougsig.actionsdispatcher.utils

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue

fun AnnotationMirror.getFieldByName(fieldName: String): AnnotationValue? {
  return elementValues.entries
    .firstOrNull { (element, _) ->
      element.simpleName.toString() == fieldName
    }
    ?.value
}