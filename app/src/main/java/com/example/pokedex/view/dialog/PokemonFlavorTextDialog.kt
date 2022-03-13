package com.example.pokedex.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.pokedex.R
import com.example.pokedex.databinding.DialogPokemonFlavorTextBinding
import com.example.pokedex.view.dialog.adapter.TextViewPager
import com.example.pokedex.view.pokemon.viewmodel.PokemonDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokemonFlavorTextDialog : DialogFragment() {

    private lateinit var binding: DialogPokemonFlavorTextBinding
    private val viewModel by activityViewModels<PokemonDetailViewModel>()
    private val args by navArgs<PokemonFlavorTextDialogArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPokemonFlavorTextBinding.inflate(inflater)
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
            binding.pokemon = it

            val texts = it.pokemonSpecie?.flavorTextEntries?.map { text ->
                TextViewPager.FragmentDetail(text, it.pokemonSpecie.color)
            } ?: emptyList()
            binding.viewPager.adapter = TextViewPager(this, texts)

            binding.viewPager.registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        binding.tab.text =
                            getString(R.string.tab_page_format, position + 1, texts.size)
                    }
                }
            )
        }
    }
}