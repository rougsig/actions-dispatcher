package com.github.rougsig.actionsdispatcher.testmodels.sample

import com.github.rougsig.actionsdispatcher.annotations.DefaultActionElement

@DefaultActionElement(DuckState::class)
sealed class DuckAction

object OpenDuckDetail : DuckAction()
object LikeDuck : DuckAction()
object DislikeDuck : DuckAction()
object AddDuckToFavorite : DuckAction()
data class UpdateLceState(val state: Any): DuckAction()
