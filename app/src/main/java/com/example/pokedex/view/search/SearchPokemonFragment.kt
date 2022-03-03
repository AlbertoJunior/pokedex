package com.example.pokedex.view.search

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.pokedex.R
import com.example.pokedex.core.EventSource
import com.example.pokedex.databinding.FragmentSearchPokemonBinding
import com.example.pokedex.view.pokemon.PokemonDetailFragment
import com.example.pokedex.view.search.viewmodel.PokemonSearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchPokemonFragment : Fragment() {

    private lateinit var binding: FragmentSearchPokemonBinding
    private val viewModel by activityViewModels<PokemonSearchViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchPokemonBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupListeners()
        callObservers()
    }

    private fun setupListeners() {
        binding.txSearch.setEndIconOnClickListener {
            binding.progress.isVisible = true
            viewModel.setPokemonId(binding.etSearch.text?.toString())
        }

        binding.etSearch.setOnKeyListener { _, code, keyEvent ->
            if (KeyEvent.ACTION_DOWN == keyEvent.action && KeyEvent.KEYCODE_ENTER == code) {
                binding.progress.isVisible = true
                viewModel.setPokemonId(binding.etSearch.text?.toString())
            }
            false
        }
    }

    private fun callObservers() {
        viewModel.pokemon.observe(viewLifecycleOwner) { eventSource ->
            binding.progress.isVisible = eventSource != null && eventSource is EventSource.Loading
            binding.fragmentContainer.isVisible =
                eventSource != null && eventSource is EventSource.Ready
            binding.tvMessage.isVisible = eventSource != null && eventSource !is EventSource.Ready

            when (eventSource) {
                is EventSource.Error -> {
                    viewModel.setPokemonId(null)
                    binding.tvMessage.text = getString(R.string.search_pokemon_not_found)
                }
                is EventSource.Loading -> {
                    binding.tvMessage.text = eventSource.message
                }
                is EventSource.Ready -> {
                    eventSource.value?.id?.let { pokemonId ->
                        replaceFragment(pokemonId)
                    }
                    viewModel.setPokemonId(null)
                }
            }
        }
    }

    private fun replaceFragment(pokemonId: Long) {
        childFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(
                R.id.fragment_container,
                PokemonDetailFragment.newInstance(pokemonId, hideBtBack = true)
            )
            .commit()
    }
}