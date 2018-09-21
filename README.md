# Actions dispatcher

# Example
To enable generation, annotate action class with `@ActionElement` annotation:
```kotlin
class State

@ActionElement(state = State::class)
sealed class Action

object OpenArticleDetail : Action()
object LikeArticle : Action()
object DislikeArticle : Action()
object AddArticleToFavorite : Action()
```

After doing that you will get an auto-generated `ActionReceiver` interface:
```kotlin
interface ActionReceiver {
    fun processAddArticleToFavorite(previousState: State, action: AddArticleToFavorite): Pair<State, Function0<Action?>?>
    fun processDislikeArticle(previousState: State, action: DislikeArticle): Pair<State, Function0<Action?>?>
    fun processLikeArticle(previousState: State, action: LikeArticle): Pair<State, Function0<Action?>?>
    fun processOpenArticleDetail(previousState: State, action: OpenArticleDetail): Pair<State, Function0<Action?>?>
}
```

And `ActionsReducer` with redcuce function:
```kotlin
class ActionsReducer private constructor(private val receiver: ActionReceiver) {
    fun redcuce(previousState: State, action: Action): Pair<State, Function0<Action?>?> = when (action) {
        is DislikeArticle -> receiver.processDislikeArticle(previousState, action)
        is OpenArticleDetail -> receiver.processOpenArticleDetail(previousState, action)
        is AddArticleToFavorite -> receiver.processAddArticleToFavorite(previousState, action)
        is LikeArticle -> receiver.processLikeArticle(previousState, action)
    }
}
```

All you have to do after adding an annotation is to use a generated builder which will create this redcucer for you and also you will need to implement a receiver:
```kotlin
class MyPresenter : BasePresenter<State, View, Action>(), ActionReceiver {
    private val reducer = ActionsReducer.Builder()
        .receiver(this) // <-- a class wich implements ActionReceiver and will receive redcuce calls
        .build()

    fun reduce(previusState: State, action: Action) {
        reducer.redcuce(previusState, action)
    }

    override fun processAddArticleToFavorite(previousState: State, action: Action): Pair<State, Function0<Action?>?> {
        // process add article to favorite
    }

    override fun processDislikeArticle(previousState: State, action: Action): Pair<State, Function0<Action?>?> {
        // process dislike article
    }

    override fun processLikeArticle(previousState: State, action: Action): Pair<State, Function0<Action?>?> {
         // process like article
    }

    override fun processOpenArticleDetail(previousState: State, action: Action): Pair<State, Function0<Action?>?> {
         // process open details article
    }
}
```

# Custom Receiver
If you don't like auto-generated receiver you can specify its interface manually.

Interface requirements:
1. Process all actions
2. Have only two parameters in this order: `state, action`
3. Your functions must return Pair<State, Function0<Action?>?> // or Pair<State, Function0<Action?>> or Pair<State, Command>

Configure processor to use this interface by adding a parameter to annotation:
```kotlin
@ActionsReducer(state = State::class, receiver = MyActionReceiver::class)
sealed class Action

object OpenArticleDetail : Action()
object LikeArticle : Action()
object DislikeArticle : Action()
object AddArticleToFavorite : Action()

interface MyActionReceiver {
  fun open(s: State, a: OpenArticleDetail): Pair<State, Function0<Action?>?>
  fun like(s: State, b: LikeArticle): Pair<State, Function0<Action?>?>
  fun dislike(s: State, c: DislikeArticle): Pair<State, Function0<Action?>?>
  fun favorite(s: State, d: AddArticleToFavorite): Pair<State, Function0<Action?>?>
}
```

# Custom Command
If you don't like default command you can specify its interface manually.

Configure processor to use this interface by adding a parameter to annotation:
```kotlin
class Command : Date()

@ActionsReducer(state = State::class, command = Command::class)
sealed class Action

object OpenArticleDetail : Action()
object LikeArticle : Action()
object DislikeArticle : Action()
object AddArticleToFavorite : Action()
```

After doing that you will get an auto-generated `ActionReceiver` interface:
```kotlin
interface ActionReceiver {
    fun processAddArticleToFavorite(previousState: State, action: Action): Pair<State, Command?>
    fun processDislikeArticle(previousState: State, action: Action): Pair<State, Command?>
    fun processLikeArticle(previousState: State, action: Action): Pair<State, Command?>
    fun processOpenArticleDetail(previousState: State, action: Action): Pair<State, Command?>
}
```

And `ActionsReducer` with redcuce function:
```kotlin
class ActionsReducer private constructor(private val receiver: ActionReceiver) {
    fun redcuce(previousState: State, action: Action): Pair<State, Command?> = when (action) {
        is DislikeArticle -> receiver.processDislikeArticle(previousState, action)
        is OpenArticleDetail -> receiver.processOpenArticleDetail(previousState, action)
        is AddArticleToFavorite -> receiver.processAddArticleToFavorite(previousState, action)
        is LikeArticle -> receiver.processLikeArticle(previousState, action)
    }
}
```

# Download

Add a Gradle dependency:

```gradle
apply plugin: 'kotlin-kapt'

sourceSets {
    main.java.srcDirs += 'src/main/kotlin'
    main.java.srcDirs += 'build/generated/source/kaptKotlin/main'           // <-- add to your module
    debug.java.srcDirs += 'build/generated/source/kaptKotlin/debug'         // <-- add to your module
    release.java.srcDirs += 'build/generated/source/kaptKotlin/release'     // <-- add to your module
    test.java.srcDirs += 'src/test/kotlin'
}

implementation 'com.github.rougsig:actions-dispatcher-annotations:1.0.7'
implementation 'com.github.rougsig:actions-dispatcher-runtime:1.0.7'
kapt 'com.github.rougsig:actions-dispatcher-processor:1.0.7'
```
