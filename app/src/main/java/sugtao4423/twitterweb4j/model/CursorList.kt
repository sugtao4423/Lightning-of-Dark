package sugtao4423.twitterweb4j.model

open class CursorList<T> : ArrayList<T>() {

    var cursorTop: String? = null
    var cursorBottom: String? = null

    fun newWithSameCursors(): CursorList<T> = CursorList<T>().also {
        it.cursorTop = cursorTop
        it.cursorBottom = cursorBottom
    }

}
