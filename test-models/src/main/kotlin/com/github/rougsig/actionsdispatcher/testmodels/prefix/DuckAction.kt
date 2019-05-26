package com.github.rougsig.actionsdispatcher.testmodels.prefix

import com.github.rougsig.actionsdispatcher.annotations.ActionElement

@ActionElement(state = DuckState::class, prefix = "execute")
sealed class DuckAction

object OpenDuckDetail : DuckAction()
object LikeDuck : DuckAction()
object DislikeDuck : DuckAction()
object AddDuckToFavorite : DuckAction()