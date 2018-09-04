package com.github.rougsig.actionsdispatcher.processor

import com.github.rougsig.actionsdispatcher.annotations.ActionElement
import com.github.rougsig.actionsdispatcher.processor.utils.Logger
import com.github.rougsig.actionsdispatcher.processor.utils.asTypeElement
import com.github.rougsig.actionsdispatcher.processor.utils.enclosedMethods
import com.github.rougsig.actionsdispatcher.processor.utils.superclass
import com.squareup.kotlinpoet.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

internal class CustomReceiverType(
  simpleName: String,
  canonicalName: String,
  packageName: String,
  processFunctions: Map<TypeElement, String>
) : ReceiverType(simpleName, canonicalName, packageName, processFunctions) {
  companion object {
    fun get(
      logger: Logger,
      receiverElement: TypeElement,
      actionType: ActionType,
      actionElementProvider: ActionElementProvider
    ): CustomReceiverType? {
      val notPresentActions = getNotPresentActions(
        logger,
        receiverElement,
        actionType,
        actionElementProvider
      ) ?: return null

      if (notPresentActions.isNotEmpty()) {
        logger.error("@ActionElement receiver function $receiverElement: " +
          "all actions must be present", receiverElement)
        notPresentActions.forEach {
          logger.error("@ActionElement receiver function $receiverElement: " +
            "Actions to add $it", receiverElement)
        }
        return null
      }

      val className = receiverElement.asClassName()
      val canonicalName = className.canonicalName
      val simpleName = className.simpleName()
      val packageName = actionType.packageName
      val processFunctions = getProcessFunctions(receiverElement)

      return CustomReceiverType(simpleName, canonicalName, packageName, processFunctions)
    }

    private fun getProcessFunctions(receiverElement: TypeElement): Map<TypeElement, String> {
      val processFunctions = HashMap<TypeElement, String>()

      receiverElement.enclosedMethods
        .forEach { method ->
          val actionName = method.parameters[1].asTypeElement()
          processFunctions[actionName] = method.simpleName.toString()
        }

      return processFunctions
    }

    private fun getNotPresentActions(
      logger: Logger,
      receiverElement: TypeElement,
      actionType: ActionType,
      actionElementProvider: ActionElementProvider
    ): List<TypeElement>? {
      val receiverActions = getReceiverActions(
        logger,
        receiverElement,
        actionType,
        actionElementProvider
      ) ?: return null

      return actionType.actions.foldRight(mutableListOf()) { elem, acc ->
        if (!receiverActions.contains(elem)) acc.add(elem)
        acc
      }
    }

    private fun getReceiverActions(
      logger: Logger,
      receiverElement: TypeElement,
      actionType: ActionType,
      actionElementProvider: ActionElementProvider
    ): List<TypeElement>? {
      val actionTypeName = actionType.name
      val stateTypeName = actionElementProvider.stateName
      val stateCommandPairTypeName = actionElementProvider.stateCommandPairName

      val receiverActions = receiverElement.enclosedMethods
        .map { method ->
          when {
            method.parameters.size != 2 -> {
              logger.error("@ActionElement receiver function $method: " +
                "must contain only 2 parameters", method)
              null
            }
            method.parameters[0].asType().asTypeName() != stateTypeName -> {
              logger.error("@ActionElement receiver function $method: " +
                "first parameter must be $stateTypeName", method)
              null
            }
            method.parameters[1].superclass.asTypeName() != actionTypeName -> {
              logger.error("@ActionElement receiver function $method: " +
                "second parameter must be $actionTypeName", method)
              null
            }
            method.returnType.asTypeName()
              .toString().replace(".jvm.functions", "") != stateCommandPairTypeName.toString() -> {
              logger.error("@ActionElement receiver function $method: " +
                "return type must be $stateCommandPairTypeName", method)
              null
            }
            else -> {
              method.parameters[1].asTypeElement()
            }
          }
        }

      val receiverActionsCount = receiverActions.size
      val filteredReceiverActions = receiverActions.filterNotNull()

      return if (receiverActionsCount != filteredReceiverActions.size) {
        null
      } else {
        filteredReceiverActions
      }
    }
  }
}