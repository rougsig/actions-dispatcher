package com.github.rougsig.actionsdispatcher.sample

import com.github.rougsig.actionsdispatcher.annotations.ActionElement

@ActionElement(state = State::class, generateDefaultReceiverImplementation = true)
internal sealed class Action

internal object OpenArticleDetail : Action()
internal object LikeArticle : Action()
internal object DislikeArticle : Action()
internal object AddArticleToFavorite : Action()

internal interface MyActionReceiver {
  fun open(s: State, a: OpenArticleDetail): Pair<State, Function0<Action>?>
  fun like(s: State, b: LikeArticle): Pair<State, Function0<Action>?>
  fun dislike(s: State, c: DislikeArticle): Pair<State, Function0<Action>?>
  fun favorite(s: State, d: AddArticleToFavorite): Pair<State, Function0<Action>?>
}

class State

typealias Command<T> = () -> T?

fun main(args: Array<String>) {
  // TODO add sample
  // Please see readme.md
}

