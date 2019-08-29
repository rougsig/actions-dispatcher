package com.github.rougsig.actionsdispatcher.kapt

class ActionDispatcherProcessorTest : APTest("com.github.rougsig.actionsdispatcher.testmodels") {
  fun testGeneration() = testProcessor(
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

  fun testName() = testProcessor(
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

  fun testPrefix() = testProcessor(
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

  fun testDefault() = testProcessor(
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

  fun testCopy() = testProcessor(
    AnnotationProcessor(
      sourceFiles = listOf("DuckAction.java"),
      expectedFiles = listOf(
        "ActionReceiver.kt.txt",
        "ActionsReducer.kt.txt"
      ),
      processor = ActionDispatcherProcessor()
    ),
    actualFileLocation = { "${it.path}/copy" }
  )
}
