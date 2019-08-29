package com.github.rougsig.actionsdispatcher.testmodels.prefix

import com.github.rougsig.actionsdispatcher.annotations.ActionElement

@ActionElement(DuckState::class, prefix = "execute")
sealed class DuckAction

object OpenDuckDetail : DuckAction()
object LikeDuck : DuckAction()
object DislikeDuck : DuckAction()
object AddDuckToFavorite : DuckAction()
data class UpdateLceState(val state: Any): DuckAction()
