package com.yousef.mysight00.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.ItemDayBinding
import java.text.SimpleDateFormat
import java.util.*

data class DayItem(
    val date: Date,
    val isSelected: Boolean = false
)

class DaysAdapter(
    private val daysList: List<DayItem>,
    private val onClick: (Date) -> Unit
) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

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

                // Update text color based on selection
                val textColor = if (dayItem.isSelected) {
                    R.color.white
                } else {
                    R.color.dark
                }
                tvDay.setTextColor(ContextCompat.getColor(root.context, textColor))
                tvDayName.setTextColor(ContextCompat.getColor(root.context, textColor))

                root.setOnClickListener { onClick(dayItem.date) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(daysList[position], onClick)
    }

    override fun getItemCount(): Int = daysList.size
}
