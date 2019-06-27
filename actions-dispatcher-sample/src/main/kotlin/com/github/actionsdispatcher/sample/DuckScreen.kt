package com.github.actionsdispatcher.sample

import com.github.rougsig.actionsdispatcher.annotations.ActionElement

@ActionElement(state = DuckState::class)
sealed class DuckAction

object OpenDuckDetails : DuckAction()
class LikeDuck : DuckAction()
data class AddDuckToFavorite(val duckId: String) : DuckAction()
