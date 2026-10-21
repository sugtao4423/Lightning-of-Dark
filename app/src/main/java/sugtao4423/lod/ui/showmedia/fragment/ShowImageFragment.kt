package sugtao4423.lod.ui.showmedia.fragment

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import sugtao4423.lod.R
import sugtao4423.lod.databinding.FragmentShowImageBinding
import sugtao4423.lod.ui.loadWithThumbnail
import sugtao4423.lod.utils.showToast

class ShowImageFragment : Fragment() {

    companion object {
        const val BUNDLE_KEY_ORIGINAL_URL = "original_url"
        const val BUNDLE_KEY_THUMBNAIL_URL = "thumbnail_url"
    }

    private lateinit var binding: FragmentShowImageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentShowImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val originalUrl = requireArguments().getString(BUNDLE_KEY_ORIGINAL_URL)!!
        val thumbnailUrl = requireArguments().getString(BUNDLE_KEY_THUMBNAIL_URL)
        val imageListener = createRequestListener(requireContext())
        binding.imageView.loadWithThumbnail(originalUrl, thumbnailUrl, imageListener)
    }

    private fun createRequestListener(context: Context) = object : RequestListener<Drawable> {

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: com.bumptech.glide.request.target.Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            context.showToast(R.string.error_get_image)
            binding.progressBar.isVisible = false
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: com.bumptech.glide.request.target.Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            binding.progressBar.isVisible = false
            return false
        }
    }

}
