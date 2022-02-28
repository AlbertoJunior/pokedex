package com.example.pokedex.view.pokedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pokedex.R
import com.example.pokedex.core.EventSource
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.databinding.FragmentListAllBinding
import com.example.pokedex.view.loading.LottieFragment
import com.example.pokedex.view.pokedex.adapter.PokemonAdapter
import com.example.pokedex.view.pokedex.listeners.PokemonAdapterListener
import com.example.pokedex.view.pokedex.viewmodel.PokemonViewModel
import com.example.pokedex.view.pokemon.PokemonDetailFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokedexFragment : Fragment() {
    private val viewModel by activityViewModels<PokemonViewModel>()

    private lateinit var binding: FragmentListAllBinding
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListAllBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        replaceFragment(LottieFragment.newInstance())
        setupAdapter()
    }

    private fun setupAdapter() {
        val pokemonAdapter = PokemonAdapter().apply {
            listener = object : PokemonAdapterListener {
                var lastPokemonId = 0L
                override fun onPokemonClicked(pokemon: Pokemon) {
                    val showingLottie =
                        requireActivity().supportFragmentManager.fragments.find { it is LottieFragment } != null

                    if (showingLottie || pokemon.id != lastPokemonId) {
                        lastPokemonId = pokemon.id
                        replaceFragment(PokemonDetailFragment.newInstance(pokemon.id))
                    } else
                        replaceFragment(LottieFragment.newInstance())
                }
            }
        }

        binding.rvPokemonList.adapter = pokemonAdapter

        viewModel.fetchPokemonList().observe(viewLifecycleOwner) {
            when (it) {
                is EventSource.Error -> {
                    binding.rvPokemonList.visibility = View.GONE
                    binding.llLoading.visibility = View.GONE
                    binding.tvMessage.text = it.message
                }
                is EventSource.Loading -> {
                    binding.rvPokemonList.visibility = View.GONE
                    binding.llLoading.visibility = View.VISIBLE
                    binding.tvFloatingMessage.text = it.message
                }
                is EventSource.Ready -> {
                    binding.rvPokemonList.visibility = View.VISIBLE
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

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}