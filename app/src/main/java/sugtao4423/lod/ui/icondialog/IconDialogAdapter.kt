package sugtao4423.lod.ui.icondialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import sugtao4423.lod.databinding.DialogIconBinding

class IconDialogAdapter(private val items: List<IconItem>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            DialogIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                .also { it.root.tag = it }
        } else {
            convertView.tag as DialogIconBinding
        }

        return binding.apply {
            val iconItem = getItem(position)
            iconText.text = iconItem.icon.toString()
            iconText.setTextColor(iconItem.iconColor)
            titleText.text = iconItem.title
        }.root
    }

    override fun getItem(position: Int): IconItem = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = items.size

}
