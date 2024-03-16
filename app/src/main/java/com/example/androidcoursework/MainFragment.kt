package com.example.androidcoursework

import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidcoursework.databinding.FragmentMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MainFragment : Fragment(), OnTimeListener {
    private var _binding: FragmentMainBinding? = null
    val MICROPHONE_REQUEST_CODE = 200
    val PERMISSIONS_REQUEST_LOCATION = 123
    private val binding get() = _binding!!
    private var perm = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var access = false
    private lateinit var recorder: MediaRecorder
    private var path = ""
    private var filename = ""
    private var isRecording = false
    private var isPausing = false
    private var duration = ""
    private var latitude: Double? = null
    private var longitude: Double? = null
    private lateinit var timer: Timer
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        access = ActivityCompat.checkSelfPermission(
            requireContext(), perm[0]
        ) == PackageManager.PERMISSION_GRANTED
        if (!access) ActivityCompat.requestPermissions(
            requireActivity(), perm, MICROPHONE_REQUEST_CODE
        )

        timer = Timer(this)

        binding.recordButton.setOnClickListener {
            when {
                isPausing -> resumeRecording()
                isRecording -> pauseRecording()
                else -> startRecording()
            }
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocation()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }

        binding.menu.setOnClickListener {
            val action = MainFragmentDirections.actionFragmentMainToCardFragment()
            findNavController().navigate(action)
        }

        binding.addButton.setOnClickListener {
            stopRecording()
            requestLocation()
            showSaveDialog()
        }

        return binding.root
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    this.latitude = location.latitude
                    this.longitude = location.longitude
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MICROPHONE_REQUEST_CODE) access =
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            access = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (access) {
                requestLocation()
            }
        }
    }

    private fun pauseRecording() {
        recorder.pause()
        isPausing = true
        binding.recordButton.setImageResource(R.drawable.recordbutton)
        timer.pause()
    }

    private fun resumeRecording() {
        recorder.resume()
        isPausing = false
        binding.recordButton.setImageResource(R.drawable.pause)
        timer.start()
    }

    private fun startRecording() {
        if (!access) {
            ActivityCompat.requestPermissions(requireActivity(), perm, MICROPHONE_REQUEST_CODE)
            return
        }
        binding.micImage.visibility = View.GONE
        binding.timer.visibility = View.VISIBLE
        recorder = MediaRecorder()
        path = "${requireActivity().externalCacheDir?.absolutePath}/"
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        val date: String = simpleDateFormat.format(Date())
        filename = "audio_record_$date"
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$path$filename.mp3")
            try {
                prepare()
            } catch (_: IOException) {
            }
            start()
        }
        binding.recordButton.setImageResource(R.drawable.pause)
        isRecording = true
        isPausing = false
        timer.start()
        binding.menu.visibility = View.GONE
        binding.addButton.visibility = View.VISIBLE
    }

    private fun stopRecording() {
        timer.stop()
        recorder.apply {
            stop()
            release()
        }
        isPausing = false
        isRecording = false
        binding.menu.visibility = View.VISIBLE
        binding.addButton.visibility = View.GONE
        binding.recordButton.setImageResource(R.drawable.recordbutton)
        binding.timer.text = "00:00:00"
    }

    override fun onTime(duration: String) {
        binding.timer.text = duration
        this.duration = duration.dropLast(0)
    }

    private fun showSaveDialog() {
        binding.timer.visibility = View.GONE
        binding.micImage.visibility = View.VISIBLE
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Сохранить запись?")

        val input = EditText(requireContext())
        input.hint = "Введите название файла"
        input.setText(filename)

        alertDialogBuilder.setView(input)
        alertDialogBuilder.setPositiveButton("Сохранить") { _, _ ->
            val fileName = input.text.toString()
            if (fileName != filename) {
                val newFile = File("$path$fileName.mp3")
                File("$path$filename.mp3").renameTo(newFile)
            }
            Toast.makeText(requireContext(), "Запись сохранена как $fileName", Toast.LENGTH_SHORT)
                .show()
            val filePath = "$path$fileName.mp3"
            val timestamp = Date().time
            viewModel.insertRecord(
                RecorderDataClass(
                    null,
                    fileName,
                    filePath,
                    timestamp,
                    duration,
                    latitude,
                    longitude
                )
            )
        }

        alertDialogBuilder.setNegativeButton("Удалить") { _, _ ->
            File("$path$filename.mp3").delete()
            Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.show()
    }

}