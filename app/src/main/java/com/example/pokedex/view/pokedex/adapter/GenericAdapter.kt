package com.example.pokedex.view.pokedex.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.core.colorByText
import com.example.pokedex.databinding.ItemGenericAdapterBinding

class GenericAdapter(private val color: String?) :
    ListAdapter<String, GenericAdapter.GenericViewHolder>(DiffCallback) {

    private object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem

        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {
        return GenericViewHolder(
            ItemGenericAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        holder.bind(getItem(position), color)
    }

    class GenericViewHolder(val binding: ItemGenericAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, color: String?) {
            binding.tvText.text = item
            binding.root.colorByText(color)
        }
    }
}
