package sugtao4423.lod.ui.adapter.user

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.lod.App
import sugtao4423.lod.databinding.ListItemUserBinding
import sugtao4423.lod.ui.adapter.converter.UserListConverter
import sugtao4423.lod.ui.loadUrl
import twitter4j.PagableResponseList
import twitter4j.User

class UserListAdapter(private val context: Context) :
    RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    private val userListViewModel = UserListViewModel(context.applicationContext as App)
    private val data = ArrayList<User>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ListItemUserBinding.inflate(inflater, viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (data.size <= position) {
            return
        }
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun addAll(users: PagableResponseList<User>) {
        val pos = data.size
        data.addAll(users)
        notifyItemRangeInserted(pos, users.size)
    }

    fun clear() {
        val size = data.size
        data.clear()
        notifyItemRangeRemoved(0, size)
    }

    inner class ViewHolder(private val binding: ListItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) = binding.apply {
            val vm = userListViewModel

            root.setOnClickListener { vm.onClickUser(it, user) }

            userIcon.loadUrl(UserListConverter.userIconUrl(user))

            userName.text = UserListConverter.userNameAndScreenName(user)
            userName.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.nameTextSize)

            val isShowProtected = UserListConverter.isShowProtected(user)
            protectIcon.visibility = if (isShowProtected) View.VISIBLE else View.GONE
            protectIcon.typeface = vm.fontAwesomeTypeface
            protectIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.protectIconSize)

            userDescription.text = UserListConverter.userDescription(user)
            userDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.textSize)

            userCountDetail.text = UserListConverter.userCountDetail(user)
            userCountDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.dateTextSize)
        }
    }

}
