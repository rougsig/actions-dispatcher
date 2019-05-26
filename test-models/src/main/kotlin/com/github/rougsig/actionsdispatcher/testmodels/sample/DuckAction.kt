package com.github.rougsig.actionsdispatcher.testmodels.sample

import com.github.rougsig.actionsdispatcher.annotations.ActionElement

@ActionElement(state = DuckState::class)
sealed class DuckAction

object OpenDuckDetail : DuckAction()
object LikeDuck : DuckAction()
object DislikeDuck : DuckAction()
object AddDuckToFavorite : DuckAction()
