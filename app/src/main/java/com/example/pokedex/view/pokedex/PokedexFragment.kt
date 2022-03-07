package com.example.pokedex.view.pokedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
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
        binding = FragmentListAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.showingLottie)
            showLottie()
        callObservers()
        setupAdapter()
    }

    private fun callObservers() {
        viewModel.popBackStackEvent.observe(viewLifecycleOwner) {
            it?.let {
                val notEmpty = childFragmentManager.fragments
                    .filterIsInstance(PokemonDetailFragment::class.java)
                    .isNotEmpty()

                if (it && notEmpty) {
                    childFragmentManager.popBackStack()
                }
                viewModel.popBackStackEvent(null)
                viewModel.setPokemonId(null)
                showLottie()
            }
        }
    }

    private fun setupAdapter() {
        val listener = object : PokemonAdapterListener {
            override fun onPokemonClicked(pokemon: Pokemon) {
                when {
                    viewModel.showingLottie -> {
                        viewModel.setPokemonId(pokemon.id)
                        replaceFragment(PokemonDetailFragment.newInstance(pokemon.id), true)
                    }
                    viewModel.lastPokemonId == pokemon.id -> {
                        viewModel.popBackStackEvent(true)
                    }
                    else -> {
                        viewModel.setPokemonId(pokemon.id)
                    }
                }
            }
        }

        val adapter = PokemonAdapter(listener)
        binding.rvPokemonList.adapter = adapter
        lifecycleScope.launch {
            viewModel.pokemonListAll.collect {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadState ->
                binding.cpProgress.isVisible = loadState.refresh is LoadState.Loading
            }
        }
    }

    private fun showLottie() {
        replaceFragment(LottieFragment.newInstance())
    }

    private fun replaceFragment(fragment: Fragment, add: Boolean = false) {
        viewModel.setLottieVisibility(fragment is LottieFragment)

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