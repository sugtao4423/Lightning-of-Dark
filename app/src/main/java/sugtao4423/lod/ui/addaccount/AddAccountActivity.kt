package sugtao4423.lod.ui.addaccount

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import sugtao4423.lod.databinding.ActivityAddAccountBinding
import sugtao4423.lod.ui.loadUrl
import sugtao4423.lod.ui.main.MainActivity

class AddAccountActivity : AppCompatActivity() {

    companion object {
        const val INTENT_KEY_EDIT_ACCOUNT_ID = "edit_account_id"
    }

    private val viewModel: AddAccountActivityViewModel by viewModels()
    private val binding by lazy { ActivityAddAccountBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }
    }

    private fun render(state: AddAccountUiState) {
        binding.getUserButton.isEnabled = !state.isLoading
        binding.saveButton.isEnabled = state.enableSaveButton

        binding.ct0Text.text = state.ct0Text
        binding.authTokenText.text = state.authTokenText
        binding.userIdText.text = state.userIdText
        binding.screenNameText.text = state.screenNameText
        state.profileImageUrl?.let {
            binding.profileImageText.text = it
            binding.profileImageView.loadUrl(it)
        } ?: run {
            binding.profileImageText.text = ""
            binding.profileImageView.setImageDrawable(null)
        }
    }

    private fun handleEvent(event: AddAccountEvent) = when (event) {
        AddAccountEvent.Finish -> finish()
        AddAccountEvent.StartMainActivity -> {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
    }

}
