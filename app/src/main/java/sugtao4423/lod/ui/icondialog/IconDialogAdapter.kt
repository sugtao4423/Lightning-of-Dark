package sugtao4423.lod.ui.icondialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import sugtao4423.lod.App
import sugtao4423.lod.databinding.DialogIconBinding

class IconDialogAdapter(
    private val context: Context,
    private val items: List<IconItem>
) : BaseAdapter() {

    private val typeface = (context.applicationContext as App).fontAwesomeTypeface

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding = if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            val tBinding = DialogIconBinding.inflate(inflater, parent, false)
            tBinding.root.tag = tBinding
            tBinding
        } else {
            convertView.tag as DialogIconBinding
        }

        return binding.also {
            it.iconItem = getItem(position) as IconItem
            it.typeface = typeface
            it.executePendingBindings()
        }.root
    }

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = items.size

}
