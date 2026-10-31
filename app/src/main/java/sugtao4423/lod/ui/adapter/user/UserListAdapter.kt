package sugtao4423.lod.ui.adapter.user

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.lod.App
import sugtao4423.lod.databinding.ListItemUserBinding
import sugtao4423.lod.ui.adapter.UpdatableListAdapter
import sugtao4423.lod.ui.loadUrl
import sugtao4423.twitter4j.User
import java.text.NumberFormat

class UserListAdapter(context: Context) : UpdatableListAdapter<User, UserListAdapter.ViewHolder>() {

    private val userListViewModel = UserListViewModel(context.applicationContext as App)
    private val numberFormat = NumberFormat.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemUserBinding.inflate(inflater, parent, false)
        val holder = ViewHolder(binding)

        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            userListViewModel.onClickUser(it, getItem(pos))
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ListItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(user: User) = binding.apply {
            val vm = userListViewModel

            userIcon.loadUrl(user.profileImage?.biggerUrl)

            userName.text = "${user.name} - @${user.screenName}"
            userName.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.nameTextSize)

            protectIcon.isVisible = user.isProtected
            protectIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.protectIconSize)

            userDescription.text = user.description
            userDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.textSize)

            userCountDetail.text = "Tweet: %s  Fav: %s  Follow: %s  Follower: %s".format(
                numberFormat.format(user.statusesCount),
                numberFormat.format(user.favoritesCount),
                numberFormat.format(user.followingCount),
                numberFormat.format(user.followersCount),
            )
            userCountDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.dateTextSize)
        }
    }

}
