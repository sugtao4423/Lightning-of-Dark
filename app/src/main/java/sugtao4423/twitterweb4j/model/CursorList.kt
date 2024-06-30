package sugtao4423.twitterweb4j.model

class CursorList<T> : ArrayList<T>() {

    var cursorTop: String? = null
    var cursorBottom: String? = null

    companion object {
        @JvmStatic
        fun <T> newWithCursor(cursorList: CursorList<T>): CursorList<T> {
            val newCursorList = CursorList<T>()
            newCursorList.cursorTop = cursorList.cursorTop
            newCursorList.cursorBottom = cursorList.cursorBottom
            return newCursorList
        }
    }

}
