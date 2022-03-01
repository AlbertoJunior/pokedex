package com.example.pokedex.view.pokedex.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.core.Utils
import com.example.pokedex.core.capitalize
import com.example.pokedex.core.colorByText
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.databinding.ItemPokemonBinding
import com.example.pokedex.view.pokedex.listeners.PokemonAdapterListener

class PokemonAdapter(val listener: PokemonAdapterListener) :
    PagingDataAdapter<Pokemon, PokemonAdapter.ViewHolder>(DiffCallback) {

    private object DiffCallback : DiffUtil.ItemCallback<Pokemon>() {
        override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon) = oldItem == newItem
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
        fun bind(pokemon: Pokemon?) {
            if (pokemon != null) {
                setupInfos(pokemon)
            } else {
                placeHolder()
            }
        }

        private fun placeHolder() {
            binding.progress.visibility = View.VISIBLE
            binding.tvName.text = "Hunting".capitalize()
            binding.ivImageFavorite.isVisible = false
        }

        private fun setupInfos(pokemon: Pokemon) {
            binding.progress.visibility = View.VISIBLE
            binding.tvName.text = pokemon.name.capitalize()
            binding.root.setOnClickListener { listener.onPokemonClicked(pokemon) }
            binding.ivImageFavorite.isVisible = pokemon.favorite
            binding.ivImageFavorite.colorByText(pokemon.pokemonSpecie?.color)
            loadImage(pokemon)
        }

        private fun loadImage(pokemon: Pokemon) {
            pokemon.getImage().let { url ->
                if (url.isNotEmpty()) {
                    Utils.loadImageGlide(
                        binding.root.context,
                        url,
                        binding.ivImage,
                        R.drawable.ic_pokemon_go,
                        listenerOnReady = {
                            binding.progress.visibility = View.GONE
                            Log.d("PokemonAdapter", "onResourceReady")
                        },
                        listenerOnError = {
                            binding.progress.visibility = View.GONE
                            Log.e("PokemonAdapter", "onLoadFailed")
                        })
                } else {
                    binding.ivImage.setImageResource(R.drawable.ic_pokemon_go)
                }
            }
        }
    }
}