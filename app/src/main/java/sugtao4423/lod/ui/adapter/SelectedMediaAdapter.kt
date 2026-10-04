package sugtao4423.lod.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.lod.R
import sugtao4423.lod.databinding.ListItemNewtweetMediaBinding
import sugtao4423.lod.entity.NewTweetMedia
import sugtao4423.lod.entity.NewTweetMediaStatus
import sugtao4423.lod.entity.NewTweetMediaType
import sugtao4423.lod.ui.loadUri
import kotlin.math.abs

class SelectedMediaAdapter(private val onChanged: (List<NewTweetMedia>) -> Unit) :
    RecyclerView.Adapter<SelectedMediaAdapter.ViewHolder>() {

    private var items = mutableListOf<NewTweetMedia>()

    private val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
        ItemTouchHelper.DOWN
    ) {
        override fun onMove(
            rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
        ): Boolean {
            val from = vh.bindingAdapterPosition
            val to = target.bindingAdapterPosition
            if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false
            items.add(to, items.removeAt(from))
            notifyItemMoved(from, to)
            return true
        }

        override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
            val pos = vh.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return
            items.removeAt(pos)
            notifyItemRemoved(pos)
        }

        override fun onChildDraw(
            c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
            dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
        ) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                vh.itemView.alpha = 1f - abs(dY) / vh.itemView.height
            }
            super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive)
        }

        override fun onSelectedChanged(vh: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(vh, actionState)
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                vh?.itemView?.alpha = 0.7f
            }
        }

        override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder) {
            super.clearView(rv, vh)
            vh.itemView.alpha = 1f
            onChanged(items.toList())
        }
    })

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        touchHelper.attachToRecyclerView(null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemNewtweetMediaBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items.size <= position) return
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submit(newItems: List<NewTweetMedia>) {
        if (newItems == items) return
        val old = items
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = old.size
            override fun getNewListSize(): Int = newItems.size
            override fun areItemsTheSame(o: Int, n: Int): Boolean = old[o].id == newItems[n].id
            override fun areContentsTheSame(o: Int, n: Int): Boolean = old[o] == newItems[n]
        })
        items = newItems.toMutableList()
        diff.dispatchUpdatesTo(this)
    }

    class ViewHolder(
        private val binding: ListItemNewtweetMediaBinding,
        private val context: Context = binding.root.context,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(media: NewTweetMedia) = binding.also {
            it.media.loadUri(media.uri)
            it.mediaType.text = when (media.type) {
                NewTweetMediaType.IMAGE -> "IMG"
                NewTweetMediaType.GIF -> "GIF"
                NewTweetMediaType.VIDEO -> "VID"
                NewTweetMediaType.UNKNOWN -> "?"
            }

            @SuppressLint("SetTextI18n")
            it.mediaSize.text = "%.2f MiB".format(media.size / 1024.0 / 1024.0)
            val bgColor = if (media.status == NewTweetMediaStatus.TOO_LARGE) {
                R.color.selectedMediaTextBackgroundError
            } else {
                R.color.selectedMediaTextBackground
            }
            it.mediaSize.setBackgroundColor(context.getColor(bgColor))
        }
    }

}
