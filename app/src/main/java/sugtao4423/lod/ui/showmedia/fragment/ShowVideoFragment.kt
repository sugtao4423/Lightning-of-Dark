package sugtao4423.lod.ui.showmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import sugtao4423.lod.R
import sugtao4423.lod.databinding.FragmentShowVideoBinding
import sugtao4423.lod.utils.showToast

class ShowVideoFragment : Fragment() {

    companion object {
        const val BUNDLE_KEY_URL = "url"
        const val BUNDLE_KEY_REPEAT = "repeat"
    }

    private lateinit var binding: FragmentShowVideoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentShowVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = requireArguments().getString(BUNDLE_KEY_URL)!!
        val repeat = requireArguments().getBoolean(BUNDLE_KEY_REPEAT, false)
        binding.playerView.player = setupVideoPlayer(url, repeat)
    }

    private fun setupVideoPlayer(url: String, repeat: Boolean): ExoPlayer =
        ExoPlayer.Builder(requireContext()).let {
            it.setSeekBackIncrementMs(5000)
            it.setSeekForwardIncrementMs(5000)
            it.build()
        }.also {
            it.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    requireContext().showToast(R.string.error_play_video)
                }
            })

            it.repeatMode = if (repeat) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF

            val mediaItem = MediaItem.fromUri(url.toUri())
            it.setMediaItem(mediaItem)
            it.prepare()
        }

    override fun onResume() {
        super.onResume()
        binding.playerView.player?.play()
    }

    override fun onPause() {
        super.onPause()
        binding.playerView.player?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.playerView.player?.release()
    }

}
