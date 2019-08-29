package com.github.rougsig.actionsdispatcher.runtime

interface BaseActionsReducer<State, Action, Command> {
  fun reduce(previousState: State, action: Action): Pair<State, Command?>
}
