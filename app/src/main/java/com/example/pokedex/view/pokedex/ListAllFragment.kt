package com.example.pokedex.view.pokedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListAllBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
    }

    private fun setupAdapter() {
        val pokemonAdapter = PokemonAdapter(object : PokemonAdapterListener {
            override fun onPokemonClicked(pokemon: Pokemon) {
                Toast.makeText(requireContext(), pokemon.name, Toast.LENGTH_SHORT).show()
            }
        })

        binding.rvPokemonList.adapter = pokemonAdapter

        binding.btFind.setOnClickListener { viewModel.fetchOnline() }

        viewModel.fetchPokemonList().observe(viewLifecycleOwner) {
            when (it) {
                is EventSource.Error -> {
                    binding.nsPokemonList.visibility = View.GONE
                    binding.tvMessage.text = it.message
                }
                is EventSource.Loading -> {
                    binding.nsPokemonList.visibility = View.GONE
                    binding.tvMessage.text = it.message
                }
                is EventSource.Ready -> {
                    binding.nsPokemonList.visibility = View.VISIBLE
                    pokemonAdapter.submitList(it.value)
                    binding.tvMessage.text = "Gotcha!"
                }
            }
        }
    }
}