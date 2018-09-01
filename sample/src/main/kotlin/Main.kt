package com.github.rougsig.actionsdispatcher.sample

import com.github.rougsig.actionsdispatcher.annotations.ActionDispatcher

@ActionDispatcher(state = State::class)
sealed class Action

object OpenArticleDetail : Action()
object LikeArticle : Action()
object DislikeArticle : Action()
object AddArticleToFavorite : Action()

class State

fun main(args: Array<String>) {
}