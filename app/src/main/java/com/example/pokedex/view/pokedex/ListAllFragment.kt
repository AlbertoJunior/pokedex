package com.example.pokedex.view.pokedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.pokedex.core.EventSource
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.databinding.FragmentListAllBinding
import com.example.pokedex.view.pokedex.adapter.PokemonAdapter
import com.example.pokedex.view.pokedex.listeners.PokemonAdapterListener
import com.example.pokedex.view.pokedex.viewmodel.PokemonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListAllFragment : Fragment() {
    private val viewModel by activityViewModels<PokemonViewModel>()

    private lateinit var binding: FragmentListAllBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListAllBinding.inflate(inflater)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
    }

    private fun setupAdapter() {
        val pokemonAdapter = PokemonAdapter().apply {
            listener = object : PokemonAdapterListener {
                override fun onPokemonClicked(pokemon: Pokemon) {
                    navController.navigate(
                        ListAllFragmentDirections
                            .actionNavigationListAllToNavigationPokemonDetails(pokemon.id)
                    )
                }
            }
        }

        binding.rvPokemonList.adapter = pokemonAdapter

        viewModel.fetchPokemonList().observe(viewLifecycleOwner) {
            when (it) {
                is EventSource.Error -> {
                    binding.nsPokemonList.visibility = View.GONE
                    binding.llLoading.visibility = View.GONE
                    binding.tvMessage.text = it.message
                }
                is EventSource.Loading -> {
                    binding.nsPokemonList.visibility = View.GONE
                    binding.llLoading.visibility = View.VISIBLE
                    binding.tvMessageLoading.text = it.message
                }
                is EventSource.Ready -> {
                    binding.nsPokemonList.visibility = View.VISIBLE
                    binding.llLoading.visibility = View.GONE

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