package ru.rougsig.actionsdispatcher.sample

import ru.rougsig.actionsdispatcher.annotations.ActionDispatcher

@ActionDispatcher(state = State::class)
sealed class Action

object A : Action()
object B : Action()
object C : Action()
object D : Action()

interface MyActionReceiver {
  fun a(a: A)
  fun processB(b: B)
  fun barC(c: C)
  fun fooD(d: D)
}

class State

fun main(args: Array<String>) {
}