package com.example.pokedex.view.pokemon.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.core.colorByText
import com.example.pokedex.core.textColorByText
import com.example.pokedex.databinding.ItemGenericAdapterBinding
import com.example.pokedex.view.dialog.listeners.GenericAdapterClickListener

class GenericAdapter(
    private val color: String?,
    private val listener: GenericAdapterClickListener? = null
) : ListAdapter<String, GenericAdapter.GenericViewHolder>(DiffCallback) {

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
            ),
            color,
            listener
        )
    }

    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class GenericViewHolder(
        private val binding: ItemGenericAdapterBinding,
        private val color: String?,
        private val onClickListener: GenericAdapterClickListener?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, position: Int) {
            binding.tvText.text = item
            binding.tvText.textColorByText(color)
            binding.root.colorByText(color)

            onClickListener?.let { listener ->
                binding.root.setOnClickListener {
                    listener.onItemClick(position)
                }
            }
        }
    }
}
