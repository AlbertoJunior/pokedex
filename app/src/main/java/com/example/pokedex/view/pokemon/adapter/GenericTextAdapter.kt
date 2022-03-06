package com.example.pokedex.view.pokemon.adapter

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.core.textColorByText
import com.google.android.material.textview.MaterialTextView

class GenericTextAdapter(
    private val color: String?,
) : ListAdapter<String, GenericTextAdapter.GenericTextViewHolder>(DiffCallback) {

    private object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem

        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericTextViewHolder {
        val text = MaterialTextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textSize = 14F
        }

        return GenericTextViewHolder(text, color)
    }

    override fun onBindViewHolder(holder: GenericTextViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GenericTextViewHolder(
        private val binding: TextView,
        private val color: String?,
    ) : RecyclerView.ViewHolder(binding) {

        fun bind(item: String) {
            binding.text = item
            binding.textColorByText(color)
        }
    }
}
