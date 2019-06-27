package com.github.rougsig.actionsdispatcher.ideaplugin

import com.github.rougsig.actionsdispatcher.compiler.ActionDispatcherGenerator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File
import java.util.*

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
    processEvent(event)
  }

  override fun fileCreated(event: VirtualFileEvent) {
    processEvent(event)
  }

  private fun processEvent(event: VirtualFileEvent) {
    val file = event.file
    if (file.extension != "kt") return

    val tokens = StringTokenizer(file.presentableUrl, "\\/")
    val path = LinkedList<String>()

    loop@ while (tokens.hasMoreTokens()) {
      val token = tokens.nextToken()
      path.add(token)
      if (token == "src") break@loop
    }

    if (path.last != "src") return
    path.removeLast()
    path.add("build")
    path.add("generated")
    path.add("source")
    path.add("kaptKotlin")
    path.add("debug")

    val outputDir = File(path.joinToString("/"))
    ActionDispatcherGenerator.process(String(file.inputStream.readBytes()), outputDir)
      .forEach {
        LocalFileSystem.getInstance()
          .findFileByIoFile(File(it))
          ?.refresh(
            /* asynchronous = */ ApplicationManager.getApplication().isReadAccessAllowed,
            /* recursive = */ true
          )
          ?: VirtualFileManager.getInstance().syncRefresh()
      }
  }
}
