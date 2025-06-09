package com.yousef.mysight00.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.ItemDayBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayItem(
    val date: Date,
    var isSelected: Boolean = false
)

class DaysAdapter(
    private var daysList: List<DayItem>,
    private val onClick: (Date) -> Unit
) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

    private var selectedPosition = -1

    init {
        // Set initial selection to today
        val today = Calendar.getInstance().time
        daysList.forEachIndexed { index, dayItem ->
            if (isSameDay(dayItem.date, today)) {
                selectedPosition = index
                dayItem.isSelected = true
            }
        }
    }

    class DayViewHolder(private val binding: ItemDayBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        private val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())

        fun bind(dayItem: DayItem, onClick: (Date) -> Unit) {
            binding.apply {
                tvDay.text = dayFormat.format(dayItem.date)
                tvDayName.text = dayNameFormat.format(dayItem.date)
                
                // Update background based on selection
                val backgroundRes = if (dayItem.isSelected) {
                    R.drawable.bg_rounded_blue_oval
                } else {
                    R.drawable.bg_rounded_gray_oval
                }
                root.setBackgroundResource(backgroundRes)

                // Update text colors based on selection
                val dayNameColor = if (dayItem.isSelected) {
                    R.color.white
                } else {
                    R.color.dark
                }
                
                val dayNumberColor = if (dayItem.isSelected) {
                    R.color.primary_blue
                } else {
                    R.color.dark
                }
                
                tvDayName.setTextColor(ContextCompat.getColor(root.context, dayNameColor))
                tvDay.setTextColor(ContextCompat.getColor(root.context, dayNumberColor))

                root.setOnClickListener { onClick(dayItem.date) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(daysList[position]) { date ->
            updateSelection(position)
            onClick(date)
        }
    }

    override fun getItemCount() = daysList.size

    private fun updateSelection(position: Int) {
        if (position == selectedPosition) return

        val previousSelected = selectedPosition
        selectedPosition = position

        // Update the previous selection
        if (previousSelected != -1) {
            daysList[previousSelected].isSelected = false
            notifyItemChanged(previousSelected)
        }

        // Update the new selection
        daysList[position].isSelected = true
        notifyItemChanged(position)
    }

    fun updateDays(newDays: List<DayItem>) {
        val oldSelectedDate = if (selectedPosition != -1) daysList[selectedPosition].date else null
        
        daysList = newDays.map { dayItem ->
            DayItem(
                date = dayItem.date,
                isSelected = oldSelectedDate != null && isSameDay(dayItem.date, oldSelectedDate)
            )
        }
        
        // Update selected position
        selectedPosition = daysList.indexOfFirst { it.isSelected }
        notifyDataSetChanged()
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
