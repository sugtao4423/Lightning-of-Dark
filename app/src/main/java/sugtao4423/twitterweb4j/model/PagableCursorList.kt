package sugtao4423.twitterweb4j.model

// Only for user following and followers list
class PagableCursorList<T> : CursorList<T>() {

    fun hasNext(): Boolean = cursorBottom == null || !cursorBottom!!.startsWith("0|")

}
