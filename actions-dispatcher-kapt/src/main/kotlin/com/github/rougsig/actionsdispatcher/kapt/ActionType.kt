package com.github.rougsig.actionsdispatcher.kapt

import com.github.rougsig.actionsdispatcher.kapt.utils.Logger
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.asTypeName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Class
import me.eugeniomarletti.kotlin.metadata.visibility
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

internal data class ActionType(
  val proto: Class,
  val element: TypeElement,
  val actions: List<TypeElement>,
  val isInternal: Boolean
) {
  val name = element.className
  val packageName = name.packageName

  companion object {
    fun get(logger: Logger, element: Element): ActionType? {
      val typeMetadata = element.kotlinMetadata
      if (element !is TypeElement || typeMetadata !is KotlinClassMetadata) {
        logger.error("@ActionElement can't be applied to $element: must be kotlin class", element)
        return null
      }

      val proto = typeMetadata.data.classProto
      when {
        proto.classKind != Class.Kind.CLASS -> {
          logger.error("@ActionReducer can't be applied to $element: must be a class", element)
          return null
        }
      }

      val actions = getActions(element)
      val isInternal = proto.visibility!! == ProtoBuf.Visibility.INTERNAL

      return ActionType(proto, element, actions, isInternal)
    }

    private fun getActions(actionTypeElement: TypeElement): List<TypeElement> {
      val actions = mutableListOf<TypeElement>()

      for (element in actionTypeElement.enclosingElement.enclosedElements) {
        val typeMetadata = element.kotlinMetadata

        if (element !is TypeElement || typeMetadata !is KotlinClassMetadata) continue
        if (element.superclass.toString() != actionTypeElement.asType().toString()) continue

        actions.add(element)
      }

      return actions
    }
  }

  private val Element.className: ClassName
    get() {
      val typeName = asType().asTypeName()
      return when (typeName) {
        is ClassName -> typeName
        is ParameterizedTypeName -> typeName.rawType
        else -> throw IllegalStateException("unexpected TypeName: ${typeName::class}")
      }
    }
}
