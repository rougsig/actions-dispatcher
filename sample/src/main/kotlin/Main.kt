package ru.rougsig.actionsdispatcher.sample

import ru.rougsig.actionsdispatcher.annotations.ActionDispatcher

@ActionDispatcher(state = State::class)
sealed class Action

object A : Action()
object B : Action()
object C : Action()
object D : Action()

interface MyActionReceiver

class State

fun main(args: Array<String>) {
}