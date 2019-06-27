package com.github.rougsig.actionsdispatcher.ideaplugin

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager

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
  }

  override fun fileCreated(event: VirtualFileEvent) {
  }
}
