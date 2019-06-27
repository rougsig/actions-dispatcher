package com.github.rougsig.actionsdispatcher.compiler

import com.github.rougsig.actionsdispatcher.parser.ActionElementModelParser
import com.squareup.kotlinpoet.ClassName
import junit.framework.TestCase
import org.junit.Test

internal class ActionElementModelMapperTest : TestCase() {
  @Test
  fun testModelMapToParams() {
    val model = ActionElementModelParser.Model(
      packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
      baseClassName = "DuckAction",
      prefix = "process",
      stateClassName = "DuckState",
      reducerName = "ActionsReducer",
      receiverName = "ActionReceiver",
      isDefaultGenerationEnabled = false,
      actions = listOf("OpenDuckDetails", "LikeDuck", "AddDuckToFavorite")
    )

    assertEquals(
      ActionDispatcherGenerator.Params(
        packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
        reducerName = "ActionsReducer",
        receiverName = "ActionReceiver",
        receiverType = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.ActionReceiver"),
        baseActionType = ClassName.bestGuess("com.github.rougsig.actionsdispatcher.testmodels.sample.DuckAction"),
        stateType = ClassName.bestGuess("DuckState"),
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
      ),
      model.mapToParams()
    )
  }
}
