package com.example.androidcoursework

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidcoursework.databinding.RecyclerActivityBinding
import java.text.SimpleDateFormat
import java.util.Date

class RecorderAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<RecorderAdapter.ViewHolder>() {

    private var editMode = false
    private var recordsList: List<RecorderDataClass> = emptyList()

    fun isEditMode(): Boolean = editMode

    fun setEditMode(mode: Boolean) {
        if (editMode != mode) {
            editMode = mode
            notifyDataSetChanged()
        }
    }

    fun setRecords(recordList: List<RecorderDataClass>) {
        this.recordsList = recordList
        notifyDataSetChanged()
    }

    inner class ViewHolder(binding: RecyclerActivityBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        private val fileNameText: TextView = binding.filename
        private val metaText: TextView = binding.meta
        private val checkBoxText: CheckBox = binding.checkbox
        private val shareButton: ImageView = binding.sharebut

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            shareButton.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClickListener(position)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemLongClickListener(position)
            }
            return true
        }

        fun bind(record: RecorderDataClass) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            val date = Date(record.timestamp)
            val strDate = dateFormat.format(date)

            fileNameText.text = record.filename

            val coordinateText = if (record.latitude != null && record.longitude != null) {
                "Широта: ${record.latitude}, Долгота: ${record.longitude}"
            } else {
                "Местоположение не обнаружено"
            }
            metaText.text = "${record.duration} $strDate\n$coordinateText"

            shareButton.setOnClickListener {
                listener.onShareClickListener(adapterPosition)
            }

            if (editMode) {
                checkBoxText.visibility = View.VISIBLE
                checkBoxText.isChecked = record.isChecked
            } else {
                checkBoxText.visibility = View.GONE
                checkBoxText.isChecked = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = recordsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(recordsList[position])
    }

}