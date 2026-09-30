package sugtao4423.lod.ui.adapter

import androidx.recyclerview.widget.RecyclerView

interface ListUpdatable<T> {
    fun add(item: T)
    fun addAll(items: List<T>)
    fun insertTop(items: List<T>)
    fun clear()
}

abstract class UpdatableListAdapter<T, VH : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<VH>(), ListUpdatable<T> {

    val data = arrayListOf<T>()

    override fun getItemCount(): Int = data.size

    override fun add(item: T) {
        data.add(item)
        notifyItemInserted(data.size - 1)
    }

    override fun addAll(items: List<T>) {
        val pos = data.size
        data.addAll(items)
        notifyItemRangeInserted(pos, items.size)
    }

    override fun insertTop(items: List<T>) {
        data.addAll(0, items)
        notifyItemRangeInserted(0, items.size)
    }

    override fun clear() {
        val size = data.size
        data.clear()
        notifyItemRangeRemoved(0, size)
    }

}
