package sugtao4423.lod.ui.addaccount

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import sugtao4423.lod.databinding.ActivityAddAccountBinding
import sugtao4423.lod.ui.loadUrl
import sugtao4423.lod.ui.main.MainActivity

class AddAccountActivity : AppCompatActivity() {

    companion object {
        const val INTENT_KEY_EDIT_ACCOUNT_ID = "edit_account_id"
    }

    private val viewModel: AddAccountActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val editAccountId = intent.getLongExtra(INTENT_KEY_EDIT_ACCOUNT_ID, -1)
        if (editAccountId != -1L) {
            viewModel.setEditAccount(editAccountId)
        }

        binding.apply {
            cookieEdit.doAfterTextChanged {
                viewModel.afterChangeCookie(it?.toString() ?: "")
            }
            getUserButton.setOnClickListener {
                viewModel.getUserInfo()
            }
            saveButton.setOnClickListener {
                viewModel.save()
            }
        }

        viewModel.isLoading.observe(this) {
            binding.getUserButton.isEnabled = !it
        }
        viewModel.enableSaveButton.observe(this) {
            binding.saveButton.isEnabled = it
        }
        viewModel.ct0Text.observe(this) {
            binding.ct0Text.text = it
        }
        viewModel.authTokenText.observe(this) {
            binding.authTokenText.text = it
        }
        viewModel.userIdText.observe(this) {
            binding.userIdText.text = it
        }
        viewModel.screenNameText.observe(this) {
            binding.screenNameText.text = it
        }
        viewModel.profileImageUrl.observe(this) {
            binding.profileImageText.text = it ?: ""
            if (it == null) {
                binding.profileImageView.setImageDrawable(null)
            } else {
                binding.profileImageView.loadUrl(it)
            }
        }
        viewModel.onFinishEvent.observe(this) {
            finish()
        }
        viewModel.onStartMainActivityEvent.observe(this) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
    }

}
