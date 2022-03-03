package com.example.pokedex.view.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.pokedex.R
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
            viewModel.setPokemonId(binding.etSearch.text?.toString())
        }
    }

    private fun callObservers() {
        viewModel.pokemon.observe(viewLifecycleOwner) { pokemon ->
            viewModel.setPokemonId(null)

            if (pokemon != null)
                replaceFragment(pokemon.id)
        }
    }

    private fun replaceFragment(pokemonId: Long) {
        childFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragment_container, PokemonDetailFragment.newInstance(pokemonId))
            .commit()
    }
}