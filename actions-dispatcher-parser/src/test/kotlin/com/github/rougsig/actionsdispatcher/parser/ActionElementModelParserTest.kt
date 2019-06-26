package com.github.rougsig.actionsdispatcher.parser

import com.github.rougsig.actionsdispatcher.entity.ActionElementModel
import junit.framework.TestCase

class ActionElementModelParserTest : TestCase() {
  fun testFindSimpleAnnotationWithoutExtraParameters() {
    val source = """
      package com.github.rougsig.actionsdispatcher.testmodels.sample

      import com.github.rougsig.actionsdispatcher.annotations.ActionElement
      
      @ActionElement(state = DuckState::class)
      sealed class DuckAction
      
      object OpenDuckDetails : DuckAction()
      class LikeDuck : DuckAction()
      data class AddDuckToFavorite(val duckId: String) : DuckAction()
    """.trimIndent()

    assertEquals(
      ActionElementModel(
        packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
        baseClassName = "DuckAction",
        prefix = "process",
        state = "DuckState",
        reducerName = "ActionsReducer",
        receiverName = "ActionReceiver",
        isDefaultGenerationEnabled = false,
        actions = listOf("OpenDuckDetails", "LikeDuck", "AddDuckToFavorite")
      ),
      parseActionElementModel(source)
    )
  }

  fun testFindSimpleAnnotationWithNameParameters() {
    val source = """
      package com.github.rougsig.actionsdispatcher.testmodels.sample
      
      import com.github.rougsig.actionsdispatcher.annotations.ActionElement
      
      @ActionElement(state = DuckState::class, receiverName = "MyReceiver", reducerName = "MyReducer")
      sealed class DuckAction

      object OpenDuckDetails : DuckAction()
      class LikeDuck : DuckAction()
      data class AddDuckToFavorite(val duckId: String) : DuckAction()
    """.trimIndent()

    assertEquals(
      ActionElementModel(
        packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
        baseClassName = "DuckAction",
        prefix = "process",
        state = "DuckState",
        reducerName = "MyReducer",
        receiverName = "MyReceiver",
        isDefaultGenerationEnabled = false,
        actions = listOf("OpenDuckDetails", "LikeDuck", "AddDuckToFavorite")
      ),
      parseActionElementModel(source)
    )
  }

  fun testFindSimpleAnnotationWithPrefixParameters() {
    val source = """
      package com.github.rougsig.actionsdispatcher.testmodels.sample
      
      import com.github.rougsig.actionsdispatcher.annotations.ActionElement
      
      @ActionElement(state = DuckState::class, prefix = "execute")
      sealed class DuckAction

      object OpenDuckDetails : DuckAction()
      class LikeDuck : DuckAction()
      data class AddDuckToFavorite(val duckId: String) : DuckAction()
    """.trimIndent()

    assertEquals(
      ActionElementModel(
        packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
        baseClassName = "DuckAction",
        prefix = "execute",
        state = "DuckState",
        reducerName = "ActionsReducer",
        receiverName = "ActionReceiver",
        isDefaultGenerationEnabled = false,
        actions = listOf("OpenDuckDetails", "LikeDuck", "AddDuckToFavorite")
      ),
      parseActionElementModel(source)
    )
  }

  fun testFindSimpleAnnotationWithDefaultGenerationParameter() {
    val source = """
      package com.github.rougsig.actionsdispatcher.testmodels.sample
      
      import com.github.rougsig.actionsdispatcher.annotations.ActionElement
      
      @ActionElement(state = DuckState::class, isDefaultGenerationEnabled = true)
      sealed class DuckAction

      object OpenDuckDetails : DuckAction()
      class LikeDuck : DuckAction()
      data class AddDuckToFavorite(val duckId: String) : DuckAction()
    """.trimIndent()

    assertEquals(
      ActionElementModel(
        packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
        baseClassName = "DuckAction",
        prefix = "process",
        state = "DuckState",
        reducerName = "ActionsReducer",
        receiverName = "ActionReceiver",
        isDefaultGenerationEnabled = true,
        actions = listOf("OpenDuckDetails", "LikeDuck", "AddDuckToFavorite")
      ),
      parseActionElementModel(source)
    )
  }
}
