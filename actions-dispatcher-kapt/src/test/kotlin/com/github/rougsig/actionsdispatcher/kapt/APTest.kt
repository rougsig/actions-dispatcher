package com.github.rougsig.actionsdispatcher.kapt

import com.google.common.collect.ImmutableList
import com.google.common.io.Files
import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import junit.framework.TestCase
import java.io.File
import java.nio.file.Paths

private const val TEST_MODELS_STUB_DIR = "test-models/build/tmp/kapt3/stubs/main"
private const val TEST_RESOURCES_DIR = "src/test/resources"

abstract class APTest(
  private val packageName: String,
  private val enforcePackage: Boolean = true
) : TestCase() {

  fun testProcessor(
    vararg processor: AnnotationProcessor,
    generationDir: File = Files.createTempDir(),
    actualFileLocation: (File) -> String = { it.path }
  ) {
    processor.forEach { (sources, expectedFiles, proc, error) ->

      require(!(expectedFiles == null && error.isNullOrBlank())) {
        "Expected files and error cannot be both null"
      }

      require(expectedFiles == null || error.isNullOrBlank()) {
        "Expected files or error must be set"
      }

      val projectRoot = File(".").absoluteFile.parentFile.parent
      val packageNameDir = packageName.replace(".", "/")

      val stubs = actualFileLocation(Paths.get(projectRoot, TEST_MODELS_STUB_DIR, packageNameDir).toFile())
      val expectedDir = Paths.get(TEST_RESOURCES_DIR, packageNameDir).toFile()

      val compilation = Compiler.javac()
        .withProcessors(proc)
        .withOptions(ImmutableList.of("-Akapt.kotlin.generated=$generationDir", "-proc:only"))
        .compile(sources.map {
          val stub = File(stubs, it).toURI().toURL()
          JavaFileObjects.forResource(stub)
        })

      if (error != null) {
        CompilationSubject
          .assertThat(compilation)
          .failed()
        CompilationSubject
          .assertThat(compilation)
          .hadErrorContaining(error)
      } else {
        CompilationSubject
          .assertThat(compilation)
          .succeeded()

        val targetDir =
          File(actualFileLocation(if (enforcePackage) File("${generationDir.absolutePath}/$packageNameDir")
          else generationDir))

        if (expectedFiles != null) {
          assertEquals(expectedFiles.size, targetDir.listFiles().size)

          expectedFiles.forEach { fileName ->
            val expectedFile = File(actualFileLocation(expectedDir), fileName)
            val actualFile = targetDir
              .listFiles()
              .find { it.name == expectedFile.nameWithoutExtension }
              ?: throw IllegalStateException(
                "expected file not generated: $fileName\n" +
                  "Generated files:\n    ${targetDir.listFiles().joinToString("\n    ")}"
              )
            assertEquals(
              expectedFile.nameWithoutExtension,
              actualFile.name
            )
            assertSameLines(
              expectedFile.readText(),
              actualFile.readText()
            )
          }
        }
      }
    }
  }

  private fun assertSameLines(expected: String, actual: String) {
    fun String.convertLineSeparators() = replace("\r\n", "\n")
    assertEquals(
      expected.trim().convertLineSeparators(),
      actual.trim().convertLineSeparators()
    )
  }
}
