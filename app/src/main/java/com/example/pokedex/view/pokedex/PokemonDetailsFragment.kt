package com.example.pokedex.view.pokedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pokedex.R
import com.example.pokedex.core.Utils
import com.example.pokedex.databinding.FragmentPokemonDetailsBinding
import com.example.pokedex.view.pokedex.viewmodel.PokemonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokemonDetailsFragment : Fragment() {
    private lateinit var binding: FragmentPokemonDetailsBinding
    private lateinit var navController: NavController

    private val args by navArgs<PokemonDetailsFragmentArgs>()
    private val viewModel by activityViewModels<PokemonViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPokemonDetailsBinding.inflate(inflater, container, false)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.hideBottomNav()
        setupListeners()
        loadPokemon()
    }

    private fun setupListeners() {
        binding.btCatch.setOnClickListener { viewModel.savePokemonFavorite(args.pokemonId) }
        binding.btFlee.setOnClickListener { navController.navigateUp() }
    }

    private fun loadPokemon() {
        viewModel.fetchPokemonUltraDetail(args.pokemonId).observe(viewLifecycleOwner) {
            binding.pokemonDetail = it

            Utils.loadImageGlide(
                requireContext(), it.getImage(), binding.ivImage, R.drawable.ic_pokeball,
                listenerOnReady = {
                    binding.progress.visibility = View.GONE
                },
                listenerOnError = {
                    binding.progress.visibility = View.GONE
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.showBottomNav()
    }
}