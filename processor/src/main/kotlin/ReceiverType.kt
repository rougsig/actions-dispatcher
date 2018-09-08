package com.github.rougsig.actionsdispatcher.processor

import com.github.rougsig.actionsdispatcher.processor.utils.Logger
import javax.lang.model.element.TypeElement

internal abstract class ReceiverType(
  val simpleName: String,
  val canonicalName: String,
  val packageName: String,
  val processFunctions: Map<TypeElement, String>
) {
  companion object {
    fun get(
      logger: Logger,
      actionType: ActionType,
      actionElementProvider: ActionElementProvider
    ): ReceiverType? {
      val customReceiverElement = actionElementProvider.customReceiverElement
      return if (customReceiverElement != null) {
        CustomReceiverType.get(
          logger,
          customReceiverElement,
          actionType,
          actionElementProvider
        )
      } else {
        AutoGeneratedReceiverType.get(
          actionType,
          actionElementProvider
        )
      }
    }
  }
}
