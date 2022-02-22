package com.example.pokedex.view.pokedex.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.example.pokedex.R
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.databinding.ItemPokemonBinding
import com.example.pokedex.view.pokedex.listeners.PokemonAdapterListener

class PokemonAdapter(var listener: PokemonAdapterListener) :
    ListAdapter<Pokemon, PokemonAdapter.ViewHolder>(DiffCallback) {

    private object DiffCallback : DiffUtil.ItemCallback<Pokemon>() {
        override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflate = ItemPokemonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(inflate, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemPokemonBinding,
        private val listener: PokemonAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pokemon: Pokemon) {
            val camelRegex = "[a-z]|( [a-z])".toRegex()
            binding.tvName.text = pokemon.name.replaceFirst(camelRegex, pokemon.name.first().uppercase())

            Glide.with(binding.root)
                .load(pokemon.sprites?.other?.officialArtwork?.frontDefault)
                .fitCenter()
                .circleCrop()
                .placeholder(R.drawable.ic_pokemon_go)
                .into(binding.ivImage)

            binding.root.setOnClickListener { listener.onPokemonClicked(pokemon) }
        }
    }
}