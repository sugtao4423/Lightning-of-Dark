package sugtao4423.lod.ui.icondialog

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AlertDialog
import sugtao4423.lod.databinding.DialogIconBinding

class IconDialog(context: Context) {

    private class IconDialogAdapter(private val items: List<IconItem>) : BaseAdapter() {

        override fun getItem(position: Int): IconItem = items[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getCount(): Int = items.size

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val context = parent.context
            val binding = if (convertView == null) {
                DialogIconBinding.inflate(LayoutInflater.from(context), parent, false)
                    .also { it.root.tag = it }
            } else {
                convertView.tag as DialogIconBinding
            }

            return binding.apply {
                val iconItem = getItem(position)
                iconText.text = context.getString(iconItem.icon)
                iconText.setTextColor(context.getColor(iconItem.iconColor))
                titleText.text = context.getString(iconItem.title)
            }.root
        }

    }

    private val builder = AlertDialog.Builder(context)

    fun setTitle(title: String): IconDialog = apply { builder.setTitle(title) }

    fun setItems(
        items: List<IconItem>,
        listener: DialogInterface.OnClickListener
    ): IconDialog = apply { builder.setAdapter(IconDialogAdapter(items), listener) }

    fun show(): AlertDialog = builder.show()

}
