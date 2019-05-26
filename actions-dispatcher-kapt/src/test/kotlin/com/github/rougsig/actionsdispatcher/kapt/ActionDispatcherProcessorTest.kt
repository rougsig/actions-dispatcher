package com.github.rougsig.actionsdispatcher.kapt

class ActionDispatcherProcessorTest : APTest("com.github.rougsig.actionsdispatcher.testmodels") {
  fun testSample() = testProcessor(
    AnnotationProcessor(
      sourceFiles = listOf("DuckAction.java"),
      expectedFiles = listOf(
        "ActionReceiver.kt.txt",
        "ActionsReducer.kt.txt"
      ),
      processor = ActionDispatcherProcessor()
    ),
    actualFileLocation = { "${it.path}/sample" }
  )

  fun testWithCustomPrefix() = testProcessor(
    AnnotationProcessor(
      sourceFiles = listOf("DuckAction.java"),
      expectedFiles = listOf(
        "ActionReceiver.kt.txt",
        "ActionsReducer.kt.txt"
      ),
      processor = ActionDispatcherProcessor()
    ),
    actualFileLocation = { "${it.path}/prefix" }
  )

  fun testWithCustomNames() = testProcessor(
    AnnotationProcessor(
      sourceFiles = listOf("DuckAction.java"),
      expectedFiles = listOf(
        "MyReceiver.kt.txt",
        "MyReducer.kt.txt"
      ),
      processor = ActionDispatcherProcessor()
    ),
    actualFileLocation = { "${it.path}/name" }
  )

  fun testWithDefaultGeneration() = testProcessor(
    AnnotationProcessor(
      sourceFiles = listOf("DuckAction.java"),
      expectedFiles = listOf(
        "ActionReceiver.kt.txt",
        "ActionsReducer.kt.txt"
      ),
      processor = ActionDispatcherProcessor()
    ),
    actualFileLocation = { "${it.path}/generation" }
  )
}
