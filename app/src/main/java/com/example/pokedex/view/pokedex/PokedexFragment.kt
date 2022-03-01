package com.example.pokedex.view.pokedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.pokedex.R
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.databinding.FragmentListAllBinding
import com.example.pokedex.view.loading.LottieFragment
import com.example.pokedex.view.pokedex.adapter.PokemonAdapter
import com.example.pokedex.view.pokedex.listeners.PokemonAdapterListener
import com.example.pokedex.view.pokedex.viewmodel.PokemonViewModel
import com.example.pokedex.view.pokemon.PokemonDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PokedexFragment : Fragment() {
    private val viewModel by activityViewModels<PokemonViewModel>()

    private lateinit var binding: FragmentListAllBinding

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
        val listener = object : PokemonAdapterListener {
            var lastPokemonId = 0L
            override fun onPokemonClicked(pokemon: Pokemon) {
                val showingLottie =
                    childFragmentManager.fragments.find { it is LottieFragment } != null

                when {
                    showingLottie -> {
                        lastPokemonId = pokemon.id
                        viewModel.setPokemonId(pokemon.id)
                        replaceFragment(PokemonDetailFragment.newInstance(pokemon.id), true)
                    }
                    lastPokemonId == pokemon.id -> {
                        lastPokemonId = 0L
                        viewModel.setPokemonId(null)
                        replaceFragment(LottieFragment.newInstance())
                    }
                    else -> {
                        lastPokemonId = pokemon.id
                        viewModel.setPokemonId(pokemon.id)
                    }
                }
            }
        }

        val adapter = PokemonAdapter(listener)
        binding.rvPokemonList.adapter = adapter
        lifecycleScope.launch {
            viewModel.pokemonListAllPaging.collect {
                binding.cpProgress.isVisible = false
                adapter.submitData(it)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, add: Boolean = false) {
        childFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .apply {
                if (add) addToBackStack(fragment.javaClass.simpleName)
            }
            .commit()
    }
}