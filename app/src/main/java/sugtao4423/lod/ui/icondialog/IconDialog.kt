package sugtao4423.lod.ui.icondialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class IconDialog(private val context: Context) {

    private val builder = AlertDialog.Builder(context)

    fun setTitle(title: String): AlertDialog.Builder = builder.setTitle(title)

    fun setItems(
        items: List<IconItem>,
        listener: DialogInterface.OnClickListener
    ): AlertDialog.Builder = builder.setAdapter(IconDialogAdapter(context, items), listener)

    fun show(): AlertDialog = builder.show()

}
