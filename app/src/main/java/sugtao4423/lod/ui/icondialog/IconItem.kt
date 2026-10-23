package sugtao4423.lod.ui.icondialog

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import sugtao4423.lod.R

data class IconItem(
    @StringRes val icon: Int,
    @ColorRes val iconColor: Int = R.color.icon,
    @StringRes val title: Int,
)
