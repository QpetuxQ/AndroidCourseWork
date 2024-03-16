package com.example.androidcoursework

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidcoursework.databinding.FragmentCardBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class CardFragment : Fragment() {
    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapterAudioRecords: RecorderAdapter
    private var isNavigationBlocked = false
    private lateinit var toolbar: MaterialToolbar

    private val viewModel: CardViewModel by viewModels { CardViewModel.Factory() }

    private lateinit var editBar: View
    private lateinit var backButton: ImageButton
    private lateinit var bottomSheet: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var editButton: ImageButton
    private lateinit var deleteButton: ImageButton
    private lateinit var deleteText: TextView
    private lateinit var editText: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        _binding = FragmentCardBinding.inflate(layoutInflater, container, false)

        toolbar = binding.toolBar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            val action = CardFragmentDirections.actionCardFragmentToFragmentMain()
            findNavController().navigate(action)
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = CardFragmentDirections.actionCardFragmentToFragmentMain()
                    findNavController().navigate(action)
                }
            })
        editButton = binding.buttonEdit
        deleteButton = binding.buttonDelete
        deleteText = binding.textdelete
        editText = binding.textedit

        editBar = binding.editBar
        backButton = binding.buttonBackto

        bottomSheet = binding.bottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        val recyclerView: RecyclerView = binding.recid
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        adapterAudioRecords = RecorderAdapter(listener).apply {
            viewModel.records.observe(viewLifecycleOwner) {
                setRecords(it)
            }
        }
        recyclerView.adapter = adapterAudioRecords

        backButton.setOnClickListener {
            exitEditMode()
        }

        deleteButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Удалить запись(-и)?")
            builder.setPositiveButton("Удалить") { _, _ ->
                viewModel.records.observe(viewLifecycleOwner) { records ->
                    val recordsToDelete = records.filter { it.isChecked }.toList()
                    viewModel.deleteRecords(recordsToDelete)
                }
                exitEditMode()
            }
            builder.setNegativeButton("Отменить") { _, _ -> }
            val dialog = builder.create()
            dialog.show()
        }

        editButton.setOnClickListener {
            showRenameDialog()
        }
        return binding.root
    }
    private fun exitEditMode() {
        if (isNavigationBlocked) {
            return
        }

        val actionBars = (requireActivity() as AppCompatActivity).supportActionBar
        actionBars?.setDisplayHomeAsUpEnabled(true)
        actionBars?.setDisplayShowHomeEnabled(true)
        editBar.visibility = View.GONE

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        viewModel.records.observe(viewLifecycleOwner) {
            it.map {
                it.isChecked = false
            }
        }
        adapterAudioRecords.setEditMode(false)

        isNavigationBlocked = true
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)
            isNavigationBlocked = false
        }
    }
    private fun switchDeleteMode(isClickable: Boolean) {
        deleteText.isClickable = isClickable
        val grayColor = ContextCompat.getColor(requireContext(), androidx.appcompat.R.color.material_blue_grey_800)
        deleteText.setTextColor(grayColor)
        deleteText.backgroundTintList = ColorStateList.valueOf(grayColor)
    }

    private fun switchEditMode(isClickable: Boolean) {
        editText.isClickable = isClickable
        val grayColor = ContextCompat.getColor(requireContext(), androidx.appcompat.R.color.material_blue_grey_800)
        editText.setTextColor(grayColor)
        editText.backgroundTintList = ColorStateList.valueOf(grayColor)
    }


    private fun showRenameDialog() {
        viewModel.records.observe(viewLifecycleOwner) {
            val selectedRecords = it.filter { it.isChecked }
            if (selectedRecords.size != 1) {
                Toast.makeText(
                    requireContext(),
                    "Можно выбрать только один файл для переименования",
                    Toast.LENGTH_SHORT
                ).show()
                return@observe
            }
            val record = selectedRecords[0]
            val input = EditText(requireContext())
            input.hint = "Введите название файла"
            input.setText(record.filename)
            val dialog =
                AlertDialog.Builder(requireContext()).setTitle("Переименовать запись?")
                    .setView(input).setPositiveButton("Сохранить") { _, _ ->
                        if (input.text.toString().isBlank()) {
                            Toast.makeText(requireContext(), "Требуется имя файла", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val imm =
                                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(view?.windowToken, 0)

                            Handler(Looper.getMainLooper()).postDelayed({
                                viewModel.updateRecord(record, input.text.toString())
                                adapterAudioRecords.notifyItemChanged(it.indexOf(record))
                                exitEditMode()
                            }, 300)
                        }
                    }.setNegativeButton("Отменить") { _, _ -> }.create()

            dialog.show()
        }
    }

    private val listener = object : OnItemClickListener {
        override fun onShareClickListener(position: Int) {
            viewModel.records.observe(viewLifecycleOwner) { records ->
                val record = records[position]
                val context = requireContext()
                val file = File(record.filepath)
                val fileUri =
                    FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_TITLE, "Поделиться аудиофайлом")
                    type = "audio/mp3"
                }

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(Intent.createChooser(shareIntent, "Поделиться через"))
            }
        }

        override fun onItemLongClickListener(position: Int) {
            val actionBars = (requireActivity() as AppCompatActivity).supportActionBar
            Toast.makeText(requireContext(), "Долгое нажатие", Toast.LENGTH_SHORT)
                .show()
            adapterAudioRecords.setEditMode(true)
            val recordToDelete = viewModel.records.value?.get(position)
            if (recordToDelete != null) {
                recordToDelete.isChecked = !recordToDelete.isChecked
            }
            adapterAudioRecords.notifyItemChanged(position)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            if (adapterAudioRecords.isEditMode() && editBar.visibility == View.GONE) {
                actionBars?.setDisplayHomeAsUpEnabled(false)
                actionBars?.setDisplayShowHomeEnabled(false)
                editBar.visibility = View.VISIBLE
                switchDeleteMode(true)
                switchEditMode(true)
            }
        }

        override fun onItemClickListener(position: Int) {
            viewModel.records.observe(viewLifecycleOwner) { records ->
                val mediaRecorder = records.getOrNull(position)

                if (mediaRecorder != null) {
                    if (adapterAudioRecords.isEditMode()) {
                        mediaRecorder.isChecked = !mediaRecorder.isChecked
                        adapterAudioRecords.notifyItemChanged(position)

                        when (records.count { it.isChecked }) {
                            0 -> {
                                switchDeleteMode(false)
                                switchEditMode(false)
                            }

                            1 -> {
                                switchDeleteMode(true)
                                switchEditMode(true)
                            }

                            else -> {
                                switchDeleteMode(true)
                                switchEditMode(false)
                            }
                        }
                    } else {
                        if (!isNavigationBlocked) {
                            val action =
                                CardFragmentDirections.actionCardFragmentToMediaPlayerFragment(
                                    mediaRecorder.filename, mediaRecorder.filepath
                                )
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }
    }

}