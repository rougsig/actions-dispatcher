package com.github.rougsig.actionsdispatcher.testmodels.name

import com.github.rougsig.actionsdispatcher.annotations.ActionElement

@ActionElement(state = DuckState::class, receiverName = "MyReceiver", reducerName = "MyReducer")
sealed class DuckAction

object OpenDuckDetail : DuckAction()
object LikeDuck : DuckAction()
object DislikeDuck : DuckAction()
object AddDuckToFavorite : DuckAction()
