package com.example.pokedex.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.pokedex.R
import com.example.pokedex.databinding.DialogPokemonAreaBinding
import com.example.pokedex.view.pokemon.viewmodel.PokemonDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokemonAreaDialog : DialogFragment() {

    private lateinit var binding: DialogPokemonAreaBinding
    private val viewModel by activityViewModels<PokemonDetailViewModel>()
    private val args by navArgs<PokemonAreaDialogArgs>()

    companion object {
        fun newInstance(pokemonId: Long, itemPosition: Int) = PokemonAreaDialog().apply {
            arguments = bundleOf(
                "pokemon_id" to pokemonId,
                "item_position" to itemPosition
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPokemonAreaBinding.inflate(inflater, container, false)
        isCancelable = true
        this.dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_stroke)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        viewModel.fetchPokemonDetail(args.pokemonId).observe(viewLifecycleOwner) {
            binding.color = it.pokemonSpecie?.color
            binding.area = it.pokemonArea[args.itemPosition]
        }
    }
}