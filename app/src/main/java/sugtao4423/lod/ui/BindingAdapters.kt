package sugtao4423.lod.ui

import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

@BindingAdapter("colorSchemeResources")
fun SwipeRefreshLayout.colorSchemeColors(colorResIds: IntArray) {
    setColorSchemeColors(*colorResIds)
}
