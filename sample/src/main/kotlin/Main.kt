package com.github.rougsig.actionsdispatcher.sample

import com.github.rougsig.actionsdispatcher.annotations.ActionDispatcher

class Command : Function<String>

@ActionDispatcher(
  state = State::class,
  receiver = MyActionReceiver::class,
  dispatcherName = "MyCustomActionsDispatcher",
  command = Command::class)
sealed class Action

object A : Action()
object B : Action()

interface MyActionReceiver {
  fun a(s: State, a: A): Pair<State, Command?>
  fun processB(s: State, b: B): Pair<State, Command>
}

class State

fun main(args: Array<String>) {
}