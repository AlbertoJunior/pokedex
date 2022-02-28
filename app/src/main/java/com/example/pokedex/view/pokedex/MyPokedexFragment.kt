package com.example.pokedex.view.pokedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pokedex.core.EventSource
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.databinding.FragmentListMyPokedexBinding
import com.example.pokedex.view.pokedex.adapter.PokemonPokedexAdapter
import com.example.pokedex.view.pokedex.listeners.PokemonAdapterListener
import com.example.pokedex.view.pokedex.viewmodel.PokemonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPokedexFragment : Fragment() {
    private val navController by lazy { findNavController() }
    private lateinit var binding: FragmentListMyPokedexBinding
    private val viewModel by activityViewModels<PokemonViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListMyPokedexBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
    }

    private fun setupAdapter() {
        val pokemonAdapter = PokemonPokedexAdapter().apply {
            listener = object : PokemonAdapterListener {
                override fun onPokemonClicked(pokemon: Pokemon) {
                    navController.navigate(
                        MyPokedexFragmentDirections.actionNavigationMyPokemonsToNavigationPokemonDetails(
                            pokemon.id
                        )
                    )
                }
            }
        }

        binding.rvPokemonList.adapter = pokemonAdapter

        viewModel.fetchFavoritePokemonList().observe(viewLifecycleOwner) {
            when (it) {
                is EventSource.Error -> {
                    binding.nsPokemonList.visibility = View.GONE
                    binding.tvMessage.text = it.message
                }
                is EventSource.Loading -> {
                    binding.nsPokemonList.visibility = View.GONE
                }
                is EventSource.Ready -> {
                    binding.nsPokemonList.visibility = View.VISIBLE

                    if (it.message?.isNotEmpty() == true) {
                        binding.tvMessage.visibility = View.VISIBLE
                        binding.tvMessage.text = it.message
                    }

                    pokemonAdapter.submitList(it.value)
                }
            }
        }
    }
}