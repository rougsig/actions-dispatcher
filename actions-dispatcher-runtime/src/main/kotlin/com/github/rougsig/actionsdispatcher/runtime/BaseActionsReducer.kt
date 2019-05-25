package com.github.rougsig.actionsdispatcher.runtime

interface BaseActionsReducer<State, Action> {
  fun reduce(previousState: State, action: Action): Pair<State, Function0<Action?>?>
}