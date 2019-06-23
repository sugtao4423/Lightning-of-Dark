package sugtao4423.icondialog

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import sugtao4423.lod.App

class IconDialog(private val context: Context) {

    private val builder = AlertDialog.Builder(context)

    fun setTitle(title: String): AlertDialog.Builder {
        return builder.setTitle(title)
    }

    fun setItems(items: Array<IconItem>, listener: DialogInterface.OnClickListener): AlertDialog.Builder {
        val adapter = IconDialogAdapter(context, items)
        return builder.setAdapter(adapter, listener)
    }

    fun show(): AlertDialog {
        return builder.show()
    }

}

class IconDialogAdapter(context: Context, items: Array<IconItem>) :
        ArrayAdapter<IconItem>(context, android.R.layout.select_dialog_item, android.R.id.text1, items) {

    private val tf = (context.applicationContext as App).getFontAwesomeTypeface()
    private val density = context.resources.displayMetrics.density

    data class ViewHolder(
            val icon: TextView,
            val text: TextView
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder
        if (view == null) {
            val dip32 = (32 * density).toInt()
            val dip8 = (8 * density).toInt()

            val iconParams = RelativeLayout.LayoutParams(dip32, dip32).apply {
                setMargins(dip8, dip8, dip8, dip8)
                addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
                addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
            }
            val icon = TextView(context).apply {
                id = 114514
                textSize = 10 * density
                gravity = Gravity.CENTER
                typeface = tf
                layoutParams = iconParams
            }

            val textParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.RIGHT_OF, icon.id)
                addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
            }
            val text = TextView(context).apply {
                layoutParams = textParams
            }

            val layout = RelativeLayout(context).apply {
                layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                addView(icon)
                addView(text)
            }

            holder = ViewHolder(icon, text)
            view = layout
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val item = getItem(position) ?: return view

        holder.icon.text = item.icon.toString()
        holder.icon.setTextColor(item.iconColor)
        holder.text.text = item.title

        return view
    }

}
