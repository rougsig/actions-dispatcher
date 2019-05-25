package com.github.rougsig.actionsdispatcher.kapt

import com.github.rougsig.actionsdispatcher.annotations.ActionElement
import com.github.rougsig.actionsdispatcher.kapt.utils.asNullable
import com.github.rougsig.actionsdispatcher.runtime.BaseActionsReducer
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types
import kotlin.reflect.KClass

internal data class ActionElementProvider(
  val prefix: String,
  val receiverName: String,
  val reducerName: String,
  val customReceiverElement: TypeElement?,
  val stateName: TypeName,
  val commandName: TypeName,
  val stateCommandPairName: TypeName,
  val nullableStateCommandPairName: TypeName,
  val baseActionsReducerName: TypeName,
  val generateDefaultReceiverImplementation: Boolean
) {
  companion object {
    fun get(actionElement: ActionElement, actionType: ActionType, types: Types): ActionElementProvider {
      val prefix = actionElement.prefix
      val receiverName = actionElement.receiverName
      val reducerName = actionElement.reducerName
      val generateDefaultReceiverImplementation = actionElement.generateDefaultReceiverImplementation
      val customReceiverElement = getReceiverElement(actionType, types)
      val stateName = getStateTypeName(actionType, types)
      val commandName = getCommandTypeName(actionType, types)

      val stateCommandPairName =
        Pair::class.asTypeName()
          .parameterizedBy(stateName, commandName)

      val nullableStateCommandPairName =
        Pair::class.asTypeName()
          .parameterizedBy(
            stateName,
            getNullableCommandTypeName(actionType, types).asNullable()
          )

      val baseActionsReducerName =
        BaseActionsReducer::class.asTypeName().parameterizedBy(
          stateName,
          actionType.name
        )

      return ActionElementProvider(
        prefix,
        receiverName,
        reducerName,
        customReceiverElement,
        stateName,
        commandName,
        stateCommandPairName,
        nullableStateCommandPairName,
        baseActionsReducerName,
        generateDefaultReceiverImplementation
      )
    }

    private fun getReceiverElement(actionType: ActionType, types: Types): TypeElement? {
      val annotation = actionType.element.getAnnotationMirror(ActionElement::class)!!
      val receiverValue = annotation.getFieldByName(ANNOTATION_RECEIVER_FIELD_NAME) ?: return null
      val receiverTypeMirror = receiverValue.value as TypeMirror
      return types.asElement(receiverTypeMirror) as TypeElement
    }

    private fun AnnotationMirror.getFieldByName(fieldName: String): AnnotationValue? {
      return elementValues.entries
        .firstOrNull { (element, _) ->
          element.simpleName.toString() == fieldName
        }
        ?.value
    }

    private fun TypeElement.getAnnotationMirror(annotationClass: KClass<*>): AnnotationMirror? {
      return annotationMirrors
        .find { it.annotationType.asElement().simpleName.toString() == annotationClass.simpleName.toString() }
    }

    private fun getStateTypeName(actionType: ActionType, types: Types): TypeName {
      val annotation = actionType.element.getAnnotationMirror(ActionElement::class)
        ?: throw IllegalArgumentException("State must by provided")
      val stateValue = annotation.getFieldByName(ANNOTATION_STATE_FIELD_NAME)
      val stateTypeMirror = stateValue!!.value as TypeMirror
      return (types.asElement(stateTypeMirror) as TypeElement).asType().asTypeName()
    }

    private fun getCommandTypeName(actionType: ActionType, types: Types): TypeName {
      val annotation = actionType.element.getAnnotationMirror(ActionElement::class)
      val receiverValue = annotation!!.getFieldByName(ANNOTATION_COMMAND_FIELD_NAME)
      val receiverTypeMirror = (receiverValue?.value as? TypeMirror)
        ?: return Function0::class.asTypeName().parameterizedBy(actionType.name)
      return (types.asElement(receiverTypeMirror) as TypeElement).asType().asTypeName()
    }

    private fun getNullableCommandTypeName(actionType: ActionType, types: Types): TypeName {
      val annotation = actionType.element.getAnnotationMirror(ActionElement::class)
      val receiverValue = annotation!!.getFieldByName(ANNOTATION_COMMAND_FIELD_NAME)
      val receiverTypeMirror = (receiverValue?.value as? TypeMirror)
        ?: return Function0::class.asTypeName().parameterizedBy(actionType.name.asNullable())
      return (types.asElement(receiverTypeMirror) as TypeElement).asType().asTypeName()
    }
  }
}

private const val ANNOTATION_RECEIVER_FIELD_NAME = "receiver"
private const val ANNOTATION_STATE_FIELD_NAME = "state"
private const val ANNOTATION_COMMAND_FIELD_NAME = "command"