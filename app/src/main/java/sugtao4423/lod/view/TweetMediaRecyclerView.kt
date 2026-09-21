package sugtao4423.lod.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.lod.R

class TweetMediaRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    init {
        isHorizontalScrollBarEnabled = true

        val a = context.obtainStyledAttributes(
            attrs, R.styleable.TweetMediaRecyclerView, defStyle, 0
        )
        val gapPx = try {
            a.getDimensionPixelSize(R.styleable.TweetMediaRecyclerView_itemGap, 0)
        } finally {
            a.recycle()
        }
        addItemDecoration(ItemGapDecoration(gapPx))

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    class ItemGapDecoration(private val gapPx: Int) : ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            if (parent.getChildAdapterPosition(view) > 0) {
                outRect.left = gapPx
            }
        }
    }

}
