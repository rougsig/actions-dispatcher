package com.github.rougsig.actionsdispatcher.ideaplugin

import com.github.rougsig.actionsdispatcher.annotations.ActionElement
import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parents
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.idea.util.module
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.getSuperNames
import org.jetbrains.uast.kotlin.KotlinUAnnotation
import org.jetbrains.uast.toUElement
import java.io.File
import java.nio.file.Paths

class ActionsDispatcherProjectComponent(
  private val project: Project
) : ProjectComponent, VirtualFileListener {
  override fun projectOpened() {
    VirtualFileManager.getInstance().addVirtualFileListener(this)
  }

  override fun projectClosed() {
    VirtualFileManager.getInstance().removeVirtualFileListener(this)
  }

  override fun contentsChanged(event: VirtualFileEvent) {
    val file = PsiManager.getInstance(project).findFile(event.file)
    if (file is KtFile) process(file)
  }

  override fun fileCreated(event: VirtualFileEvent) {
    val file = PsiManager.getInstance(project).findFile(event.file)
    if (file is KtFile) process(file)
  }

  private fun parseParams(file: KtFile): ActionDispatcherGenerator.Params? {
    val (baseActionClass, actionElementAnnotation) = PsiTreeUtil
      .findChildrenOfType(file, KtClass::class.java)
      .filter { it.isSealed() }
      .mapNotNull { ktClass ->
        val annotations = PsiTreeUtil
          .findChildrenOfType(ktClass, KtAnnotationEntry::class.java)
          .mapNotNull { it.toUElement() as? KotlinUAnnotation }
          .filter { it.qualifiedName == ActionElement::class.qualifiedName!! }

        if (annotations.isNotEmpty()) {
          ktClass to annotations.first()
        } else {
          null
        }
      }
      .firstOrNull()
      ?: return null

    val processFunPrefix = actionElementAnnotation.getAttr("prefix") ?: ActionElement.DEFAULT_PREFIX
    val actions = getActions(baseActionClass, processFunPrefix)

    val packageName = file.packageFqName.asString()
    val reducerName = actionElementAnnotation.getAttr("reducerName") ?: ActionElement.DEFAULT_REDUCER_NAME
    val receiverName = actionElementAnnotation.getAttr("receiverName") ?: ActionElement.DEFAULT_RECEIVER_NAME
    val receiverType = ClassName.bestGuess("$packageName.$receiverName")
    val baseActionType = ClassName.bestGuess(baseActionClass.fqName!!.asString())
    val stateType = ClassName.bestGuess(actionElementAnnotation.getAttr("state") ?: return null)
    val isInternal = baseActionClass.hasModifier(KtTokens.INTERNAL_KEYWORD)
    val isDefaultGenerationEnabled = actionElementAnnotation
      .getAttr("isDefaultGenerationEnabled")
      ?.toBoolean() ?: false

    return ActionDispatcherGenerator.Params(
      packageName = packageName,
      reducerName = reducerName,
      receiverName = receiverName,
      receiverType = receiverType,
      baseActionType = baseActionType,
      stateType = stateType,
      isInternal = isInternal,
      isDefaultGenerationEnabled = isDefaultGenerationEnabled,
      actions = actions
    )
  }

  private fun KotlinUAnnotation.getAttr(name: String): String? {
    return attributeValues
      .find { it.name == name }
      ?.expression
      ?.asSourceString()
      ?.replace("\"", "")
  }

  private fun getActions(
    baseActionClass: KtClass,
    processFunPrefix: String
  ): List<ActionDispatcherGenerator.Params.Action> {
    val file = baseActionClass.containingKtFile
    return PsiTreeUtil
      .findChildrenOfType(file, KtClassOrObject::class.java)
      .toList()
      .filter { declaration ->
        declaration
          .getSuperNames()
          .contains(baseActionClass.fqName?.shortName()?.asString())
      }
      .map { declaration ->
        val actionName = declaration.fqName!!.shortName().asString()
        ActionDispatcherGenerator.Params.Action(
          name = actionName,
          type = ClassName.bestGuess(declaration.fqName!!.asString()),
          processFunName = "$processFunPrefix$actionName"
        )
      }
  }

  private fun process(ktFile: KtFile) {
    val generatorParams = parseParams(ktFile) ?: return

    val receiverFileSpec = ActionDispatcherGenerator.generateActionReceiver(generatorParams)
    val actionReducerFileSpec = ActionDispatcherGenerator.generateActionReducer(generatorParams)

    val dir = ktFile
      .containingFile
      .containingDirectory
      .getRootModuleDirectory()

    val outputFolder = Paths
      .get(
        dir.virtualFile.presentableUrl,
        "build",
        "generated",
        "source",
        "kaptKotlin",
        "main"
      )
      .toFile()
    outputFolder.mkdirs()

    writeToDisk(outputFolder, receiverFileSpec)
    writeToDisk(outputFolder, actionReducerFileSpec)

    LocalFileSystem.getInstance()
      .findFileByIoFile(outputFolder)
      ?.refresh(
        /* asynchronous = */ ApplicationManager.getApplication().isReadAccessAllowed,
        /* recursive = */ true
      )
      ?: VirtualFileManager.getInstance().syncRefresh()
  }

  private fun PsiDirectory.getRootModuleDirectory(): PsiDirectory {
    val projectDir = project.guessProjectDir()!!
    val projectDirParents = manager.findDirectory(projectDir)!!.parents().toList()
    val currentDirParent = parents().toList()
    val parentsDiff = currentDirParent.minus(projectDirParents).reversed()
    val moduleDir = projectDir.findChild(module!!.name)
    return moduleDir
      ?.let { manager.findDirectory(it)!! }
      ?: parentsDiff.firstOrNull() as? PsiDirectory
      ?: manager.findDirectory(projectDir)!!
  }

  private fun writeToDisk(outputFolder: File, fileSpec: FileSpec) {
    val fileName = fileSpec.members.filterIsInstance<TypeSpec>().first().name!!
    val outputDir = File(outputFolder, fileName)
    fileSpec.writeTo(outputDir)
  }
}
