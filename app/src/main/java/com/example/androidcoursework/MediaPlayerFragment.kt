package com.example.androidcoursework

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.androidcoursework.databinding.FragmentMediaPlayerBinding
import com.google.android.material.appbar.MaterialToolbar

class MediaPlayerFragment : Fragment() {
    private var _binding: FragmentMediaPlayerBinding? = null
    private val binding get() = _binding!!
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playButton: ImageButton
    private lateinit var toolbar: MaterialToolbar
    private lateinit var fileNameTextView: TextView
    private lateinit var progressLeft: TextView
    private lateinit var progressRight: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var updateSeekBar: Runnable
    private val handler = Handler(Looper.getMainLooper())
    private val args by navArgs<MediaPlayerFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaPlayerBinding.inflate(inflater, container, false)

        toolbar = binding.toolBar
        fileNameTextView = binding.filenameText
        progressLeft = binding.progressLeft

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val fileName = args.filename
        val filePath = args.filepath
        fileNameTextView.text = fileName

        mediaPlayer = MediaPlayer().apply {
            setDataSource(requireContext(), filePath.toUri())
            prepare()
        }

        progressLeft.text = formatDuration(mediaPlayer.duration)
        playButton = binding.playAudio
        seekBar = binding.seekbar

        updateSeekBar = Runnable {
            seekBar.progress = mediaPlayer.currentPosition
            progressLeft.text = formatDuration(mediaPlayer.currentPosition)
            handler.postDelayed(updateSeekBar, SEEK_BAR_UPDATE_INTERVAL)
        }

        playButton.setOnClickListener {
            togglePlayback()
        }

        mediaPlayer.setOnCompletionListener {
            stopPlayback()
        }

        seekBar.max = mediaPlayer.duration
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun togglePlayback() {
        if (mediaPlayer.isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        mediaPlayer.start()
        playButton.background = ResourcesCompat.getDrawable(
            resources, R.drawable.pausemedia, requireContext().theme
        )
        handler.postDelayed(updateSeekBar, SEEK_BAR_UPDATE_INTERVAL)
    }

    private fun pausePlayback() {
        mediaPlayer.pause()
        playButton.background = ResourcesCompat.getDrawable(
            resources, R.drawable.playmedia, requireContext().theme
        )
        handler.removeCallbacks(updateSeekBar)
    }

    private fun stopPlayback() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        playButton.background = ResourcesCompat.getDrawable(
            resources, R.drawable.playmedia, requireContext().theme
        )
        handler.removeCallbacks(updateSeekBar)
    }

    private fun formatDuration(duration: Int): String {
        val dur = duration / 1000
        val sec = dur % 60
        val min = dur / 60 % 60
        val hour = dur / 3600
        return String.format("%02d:%02d:%02d", hour, min, sec)
    }

    companion object {
        private const val SEEK_BAR_UPDATE_INTERVAL = 1000L
    }
}