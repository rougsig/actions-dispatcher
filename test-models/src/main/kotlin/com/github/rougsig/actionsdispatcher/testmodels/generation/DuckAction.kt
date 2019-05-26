package com.github.rougsig.actionsdispatcher.testmodels.generation

import com.github.rougsig.actionsdispatcher.annotations.ActionElement

@ActionElement(state = DuckState::class, isDefaultGenerationEnabled = true)
sealed class DuckAction

object OpenDuckDetail : DuckAction()
object LikeDuck : DuckAction()
object DislikeDuck : DuckAction()
object AddDuckToFavorite : DuckAction()
