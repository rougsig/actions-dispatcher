package com.github.rougsig.actionsdispatcher.parser

import java.util.*

object ActionElementModelParser {
  fun parse(source: String): Model? {
    val iterator = source.lines().iterator()
    if (!iterator.hasNext()) return null

    val packageLine = findPackageLine(iterator) ?: return null
    val (annotationLine, baseClassLine) = findAnnotationLine(iterator) ?: return null
    val classDeclarationLines = findClassDeclarationLines(iterator) ?: return null

    val packageName = parsePackageLine(packageLine)
    val annotationParams = parseAnnotationLine(annotationLine)
    val baseClassName = parseBaseClassName(baseClassLine)
    val classDeclarations = parseClassDeclarations(classDeclarationLines, baseClassName)

    return Model(
      packageName = packageName,
      baseClassName = baseClassName,

      stateClassName = annotationParams["state"] ?: return null,
      prefix = annotationParams.getOrDefault("prefix", "process"),
      reducerName = annotationParams.getOrDefault("reducerName", "ActionsReducer"),
      receiverName = annotationParams.getOrDefault("receiverName", "ActionReceiver"),
      isDefaultGenerationEnabled = annotationParams.getOrDefault("isDefaultGenerationEnabled", "false").toBoolean(),

      actions = classDeclarations
    )
  }

  data class Model(
    val packageName: String,
    val baseClassName: String,
    val isInternal: Boolean = true,

    val stateClassName: String,
    val prefix: String,
    val reducerName: String,
    val receiverName: String,
    val isDefaultGenerationEnabled: Boolean,

    val actions: List<String>
  )


  private fun parseClassDeclarations(classDeclarationLines: List<String>, baseClassName: String): List<String> {
    val superClassDeclaration = "$baseClassName()"

    return classDeclarationLines
      .filter { it.takeLast(superClassDeclaration.length) == superClassDeclaration }
      .map { line ->
        line
          .removePrefix("object")
          .removePrefix("data class")
          .removePrefix("class")
          .trim()
          .let { it.take(it.indexOfFirst { c -> c == '(' || c == ' ' }) }
      }
  }

  private fun parseBaseClassName(baseClassLine: String): String {
    return baseClassLine.removePrefix("sealed class").trim()
  }

  private fun parseAnnotationLine(annotationLine: String): Map<String, String> {
    return annotationLine
      .removePrefix("@ActionElement(")
      .dropLast(1)
      .split(",")
      .map { it.split("=") }
      .map { it[0].trim() to it[1].trim().replace(Regex("::class|\""), "") }
      .toMap()
  }

  private fun parsePackageLine(packageLine: String): String {
    return packageLine.removePrefix("package").trim()
  }

  private fun findAnnotationLine(iterator: Iterator<String>): Pair<String, String>? {
    while (iterator.hasNext()) {
      val line = iterator.next()
      if (line.startsWith("@ActionElement")) {
        return line to iterator.next()
      }
    }
    return null
  }

  private fun findPackageLine(iterator: Iterator<String>): String? {
    while (iterator.hasNext()) {
      val line = iterator.next()
      if (line.startsWith("package")) return line
    }
    return null
  }

  private fun findClassDeclarationLines(iterator: Iterator<String>): List<String>? {
    val declarations = LinkedList<String>()

    while (iterator.hasNext()) {
      val line = iterator.next()
      when {
        line.startsWith("object") -> declarations.add(line)
        line.startsWith("data class") -> declarations.add(line)
        line.startsWith("class") -> declarations.add(line)
      }
    }

    return if (declarations.isNotEmpty()) declarations else null
  }

}
