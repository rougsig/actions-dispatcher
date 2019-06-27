package com.github.rougsig.actionsdispatcher.compiler

import com.squareup.kotlinpoet.ClassName
import junit.framework.TestCase

internal class ActionDispatcherGeneratorTest : TestCase() {
  fun testActionDispatcherReceiverGenerator() {
    val params = ActionDispatcherGenerator.Params(
      packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
      reducerName = "ActionsReducer",
      receiverName = "ActionReceiver",
      receiverType = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.ActionReceiver"),
      baseActionType = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.DuckAction"),
      stateType = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.DuckScreen.DuckState"),
      isInternal = true,
      isDefaultGenerationEnabled = false,
      actions = listOf(
        ActionDispatcherGenerator.Params.Action(
          type = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.OpenDuckDetails"),
          processFunName = "processOpenDuckDetails"
        ),
        ActionDispatcherGenerator.Params.Action(
          type = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.LikeDuck"),
          processFunName = "processLikeDuck"
        ),
        ActionDispatcherGenerator.Params.Action(
          type = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.AddDuckToFavorite"),
          processFunName = "processAddDuckToFavorite"
        )
      )
    )

    assertEquals(
      """
      package com.github.rougsig.actionsdispatcher.testmodels.sample
      
      import kotlin.Function0
      import kotlin.Pair
      
      internal interface ActionReceiver {
          fun processOpenDuckDetails(previousState: DuckScreen.DuckState, action: OpenDuckDetails):
                  Pair<DuckScreen.DuckState, Function0<DuckAction?>?>
      
          fun processLikeDuck(previousState: DuckScreen.DuckState, action: LikeDuck):
                  Pair<DuckScreen.DuckState, Function0<DuckAction?>?>
      
          fun processAddDuckToFavorite(previousState: DuckScreen.DuckState, action: AddDuckToFavorite):
                  Pair<DuckScreen.DuckState, Function0<DuckAction?>?>
      }
      
      """.trimIndent(),
      ActionDispatcherGenerator.generateActionReceiver(params).toString()
    )
  }

  fun testActionDispatcherReducerGenerator() {
    val params = ActionDispatcherGenerator.Params(
      packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
      reducerName = "ActionsReducer",
      receiverName = "ActionReceiver",
      receiverType = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.ActionReceiver"),
      baseActionType = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.DuckAction"),
      stateType = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.DuckScreen.DuckState"),
      isInternal = true,
      isDefaultGenerationEnabled = false,
      actions = listOf(
        ActionDispatcherGenerator.Params.Action(
          type = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.OpenDuckDetails"),
          processFunName = "processOpenDuckDetails"
        ),
        ActionDispatcherGenerator.Params.Action(
          type = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.LikeDuck"),
          processFunName = "processLikeDuck"
        ),
        ActionDispatcherGenerator.Params.Action(
          type = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.AddDuckToFavorite"),
          processFunName = "processAddDuckToFavorite"
        )
      )
    )

    assertEquals(
      """
      package com.github.rougsig.actionsdispatcher.testmodels.sample
      
      import com.github.rougsig.actionsdispatcher.runtime.BaseActionsReducer
      import java.lang.IllegalStateException
      import kotlin.Function0
      import kotlin.Pair
      
      internal class ActionsReducer private constructor(private val receiver: ActionReceiver) :
              BaseActionsReducer<DuckScreen.DuckState, DuckAction> {
          override fun reduce(previousState: DuckScreen.DuckState, action: DuckAction):
                  Pair<DuckScreen.DuckState, Function0<DuckAction?>?> = when (action) {
              is OpenDuckDetails -> receiver.processOpenDuckDetails(previousState, action)
              is LikeDuck -> receiver.processLikeDuck(previousState, action)
              is AddDuckToFavorite -> receiver.processAddDuckToFavorite(previousState, action)
          }
      
          class Builder {
              private var receiver: ActionReceiver? = null
      
              fun receiver(receiver: ActionReceiver): Builder {
                  this.receiver = receiver
                  return this
              }
      
              fun build(): ActionsReducer {
                  if (this.receiver == null) {
                      throw
                              IllegalStateException("no target specified, use receiver Builder`s method to set it")
                  }
                  return ActionsReducer(this.receiver!!)
              }
          }
      }

      """.trimIndent(),
      ActionDispatcherGenerator.generateActionReducer(params).toString()
    )
  }
}
