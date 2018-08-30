package ru.rougsig.actionsdispatcher.sample

import ru.rougsig.actionsdispatcher.annotations.ActionDispatcher

@ActionDispatcher(
  state = State::class,
  receiver = MyActionReceiver::class,
  dispatcherName = "MyCustomActionsDispatcher")
sealed class Action

object A : Action()
object B : Action()
object C : Action()
object D : Action()

interface MyActionReceiver {
  fun a(s: State, a: A): Pair<State, Action?>
  fun processB(s: State, b: B): Pair<State, Action?>
  fun barC(s: State, c: C): Pair<State, Action?>
  fun fooD(s: State, d: D): Pair<State, Action?>
}

class State

fun main(args: Array<String>) {
}