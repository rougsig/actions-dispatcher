package com.github.rougsig.actionsdispatcher.testmodels.copy

import com.github.rougsig.actionsdispatcher.annotations.CopyActionElement

@CopyActionElement(DuckState::class)
sealed class DuckAction

object OpenDuckDetail : DuckAction()
object LikeDuck : DuckAction()
object DislikeDuck : DuckAction()
object AddDuckToFavorite : DuckAction()
data class UpdateLceState(val lceState: Any): DuckAction()
