package com.github.rougsig.actionsdispatcher.parser

import junit.framework.TestCase

internal class ActionElementModelParserTest : TestCase() {
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
      ActionElementModelParser.Model(
        packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
        baseClassName = "DuckAction",
        prefix = "process",
        stateClassName = "DuckState",
        reducerName = "ActionsReducer",
        receiverName = "ActionReceiver",
        isDefaultGenerationEnabled = false,
        actions = listOf("OpenDuckDetails", "LikeDuck", "AddDuckToFavorite")
      ),
      ActionElementModelParser.parse(source)
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
      ActionElementModelParser.Model(
        packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
        baseClassName = "DuckAction",
        prefix = "process",
        stateClassName = "DuckState",
        reducerName = "MyReducer",
        receiverName = "MyReceiver",
        isDefaultGenerationEnabled = false,
        actions = listOf("OpenDuckDetails", "LikeDuck", "AddDuckToFavorite")
      ),
      ActionElementModelParser.parse(source)
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
      ActionElementModelParser.Model(
        packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
        baseClassName = "DuckAction",
        prefix = "execute",
        stateClassName = "DuckState",
        reducerName = "ActionsReducer",
        receiverName = "ActionReceiver",
        isDefaultGenerationEnabled = false,
        actions = listOf("OpenDuckDetails", "LikeDuck", "AddDuckToFavorite")
      ),
      ActionElementModelParser.parse(source)
    )
  }

  fun testFindSimpleAnnotationWithDefaultGenerationParameter() {
    val source = """
      package com.github.rougsig.actionsdispatcher.testmodels.sample
      
      import com.github.rougsig.actionsdispatcher.annotations.ActionElement
      
      @ActionElement(state = DuckState::class, isDefaultGenerationEnabled = true)
      internal sealed class DuckAction

      internal object OpenDuckDetails : DuckAction()
      internal class LikeDuck : DuckAction()
      internal data class AddDuckToFavorite(val duckId: String) : DuckAction()
    """.trimIndent()

    assertEquals(
      ActionElementModelParser.Model(
        packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
        baseClassName = "DuckAction",
        prefix = "process",
        stateClassName = "DuckState",
        reducerName = "ActionsReducer",
        receiverName = "ActionReceiver",
        isDefaultGenerationEnabled = true,
        actions = listOf("OpenDuckDetails", "LikeDuck", "AddDuckToFavorite")
      ),
      ActionElementModelParser.parse(source)
    )
  }

  fun testFindSimpleAnnotationWithStateImport() {
    val source = """
      package com.github.rougsig.actionsdispatcher.testmodels.sample
      
      import com.github.rougsig.actionsdispatcher.annotations.ActionElement
      import com.github.rougsig.actionsdispatcher.annotations.DuckScreen.DuckState
      
      @ActionElement(state = DuckState::class, isDefaultGenerationEnabled = true)
      internal sealed class DuckAction

      internal object OpenDuckDetails : DuckAction()
      internal class LikeDuck : DuckAction()
      internal data class AddDuckToFavorite(val duckId: String) : DuckAction()
    """.trimIndent()

    assertEquals(
      ActionElementModelParser.Model(
        packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
        baseClassName = "DuckAction",
        prefix = "process",
        stateClassName = "com.github.rougsig.actionsdispatcher.annotations.DuckScreen.DuckState",
        reducerName = "ActionsReducer",
        receiverName = "ActionReceiver",
        isDefaultGenerationEnabled = true,
        actions = listOf("OpenDuckDetails", "LikeDuck", "AddDuckToFavorite")
      ),
      ActionElementModelParser.parse(source)
    )
  }

  fun testFindSimpleAnnotationWithStateAliasImport() {
    val source = """
      package com.github.rougsig.actionsdispatcher.testmodels.sample
      
      import com.github.rougsig.actionsdispatcher.annotations.ActionElement
      import com.github.rougsig.actionsdispatcher.annotations.DuckScreen.DuckState as VS
      
      @ActionElement(state = VS::class, isDefaultGenerationEnabled = true)
      internal sealed class DuckAction

      internal object OpenDuckDetails : DuckAction()
      internal class LikeDuck : DuckAction()
      internal data class AddDuckToFavorite(val duckId: String) : DuckAction()
    """.trimIndent()

    assertEquals(
      ActionElementModelParser.Model(
        packageName = "com.github.rougsig.actionsdispatcher.testmodels.sample",
        baseClassName = "DuckAction",
        prefix = "process",
        stateClassName = "com.github.rougsig.actionsdispatcher.annotations.DuckScreen.DuckState",
        reducerName = "ActionsReducer",
        receiverName = "ActionReceiver",
        isDefaultGenerationEnabled = true,
        actions = listOf("OpenDuckDetails", "LikeDuck", "AddDuckToFavorite")
      ),
      ActionElementModelParser.parse(source)
    )
  }
}
