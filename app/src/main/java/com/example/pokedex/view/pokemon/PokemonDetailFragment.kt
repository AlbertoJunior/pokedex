package com.example.pokedex.view.pokemon

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.GridLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pokedex.R
import com.example.pokedex.core.EventSource
import com.example.pokedex.core.Utils
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.databinding.FragmentPokemonDetailsBinding
import com.example.pokedex.view.pokedex.viewmodel.PokemonViewModel
import com.example.pokedex.view.pokemon.viewmodel.PokemonDetailViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokemonDetailFragment : Fragment() {
    private lateinit var binding: FragmentPokemonDetailsBinding
    private val navController by lazy { findNavController() }

    private val args by navArgs<PokemonDetailFragmentArgs>()
    private val viewModel by activityViewModels<PokemonViewModel>()
    private val viewModelDetails by activityViewModels<PokemonDetailViewModel>()

    companion object {
        fun newInstance(
            pokemonId: Long,
            hideNav: Boolean = false,
            hideButtons: Boolean = true
        ) = PokemonDetailFragment().apply {
            arguments = bundleOf(
                "pokemon_id" to pokemonId,
                "hide_nav" to hideNav,
                "hide_buttons" to hideButtons
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPokemonDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.hideNav) {
            viewModel.hideBottomNav()
        }

        binding.containerButtons.isVisible = !args.hideButtons

        setupListeners()
        loadPokemon()
        callObservers()
    }

    private fun callObservers() {
        viewModelDetails.favoriteEvent.observe(viewLifecycleOwner) { eventSource ->
            binding.mcLoadContainer.isVisible = eventSource != null
            binding.progress.isVisible = eventSource is EventSource.Loading
            binding.ivInfoLoading.isVisible = false

            eventSource?.let { event ->
                binding.tvLoadMessage.text = event.message ?: ""

                if (event !is EventSource.Loading) {
                    binding.ivInfoLoading.isVisible = true

                    val icon = when (event) {
                        is EventSource.Error -> R.drawable.ic_alert_circle
                        is EventSource.Ready -> if (event.value) R.drawable.ic_pokeball else R.drawable.ic_flee
                        else -> R.drawable.ic_alert_circle
                    }
                    binding.ivInfoLoading.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), icon)
                    )

                    if (event is EventSource.Ready) {
                        Utils.fade(event.value, binding.ivImageFavorite)
                    }

                    Utils.fade(
                        false,
                        binding.mcLoadContainer, binding.ivInfoLoading,
                        duration = 800,
                        delay = 800,
                        listenerOnAnimationEnd = {
                            viewModelDetails.clearFavoriteEvent()
                        })
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        binding.btCatch.setOnClickListener {
            viewModelDetails.savePokemonFavorite(args.pokemonId)
        }

        binding.btHelp.setOnClickListener {
            Snackbar.make(
                requireContext(),
                binding.root,
                "Double tap on image to catch or release this Pokemon",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        val gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(p0: MotionEvent?): Boolean {
                    viewModelDetails.savePokemonFavorite(args.pokemonId)
                    return true
                }
            })
        binding.ivImage.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        binding.btSend.setOnClickListener {
            viewModelDetails.sendPokemonInfo(args.pokemonId)
                .observe(viewLifecycleOwner) { eventSource ->
                    binding.mcLoadContainer.isVisible = eventSource != null
                    binding.progress.isVisible = eventSource is EventSource.Loading
                    binding.ivInfoLoading.isVisible = false

                    eventSource?.let { event ->
                        binding.tvLoadMessage.text = event.message ?: ""

                        if (event !is EventSource.Loading) {
                            binding.ivInfoLoading.isVisible = true

                            val icon = when (event) {
                                is EventSource.Error -> R.drawable.ic_alert_circle
                                is EventSource.Ready -> R.drawable.ic_cloud_upload
                                else -> R.drawable.ic_alert_circle
                            }
                            binding.ivInfoLoading.setImageDrawable(
                                ContextCompat.getDrawable(requireContext(), icon)
                            )

                            Utils.fade(
                                false,
                                binding.mcLoadContainer,
                                binding.ivInfoLoading,
                                duration = 800,
                                delay = 800
                            )
                        }
                    }
                }
        }

        binding.btBack.setOnClickListener {
            if (args.hideButtons)
                requireActivity().onBackPressed()
            else
                navController.navigateUp()
        }
    }

    private fun loadPokemon() {
        viewModelDetails.fetchPokemonUltraDetail(args.pokemonId).observe(viewLifecycleOwner) {
            binding.pokemonDetail = it

            binding.ivImageFavorite.isVisible = it.favorite

            mountChipGroup(binding.cgType, it.types)
            mountChipGroup(binding.cgEncounterGroup, it.pokemonArea.map { area -> area.name })
            mountGridGroup(it.abilities, binding.glAbilities)
            mountGridGroup(it.moves, binding.glMoves)
            mountStats(it)

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

    private fun mountGridGroup(list: List<String>?, gridLayout: GridLayout) {
        list?.let { abilities ->
            gridLayout.removeAllViews()
            gridLayout.isVisible = abilities.isNotEmpty()

            abilities.forEach { move ->
                val button = MaterialButton(requireContext()).apply {
                    text = move
                    layoutParams = GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    ).apply {
                        marginStart = 4
                        marginEnd = 4
                    }
                }
                gridLayout.addView(button)
            }
        }
    }

    private fun mountStats(pokemon: Pokemon) {
        pokemon.stats?.let { stats ->
            binding.llStatsGroup.removeAllViews()
            binding.llStatsGroup.isVisible = stats.isNotEmpty()

            stats.forEach {
                val textView = MaterialTextView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    textSize = 14F

                    val statsName = it.name
                    text = getString(R.string.stat_format, statsName, it.base.toString())
                }
                binding.llStatsGroup.addView(textView)
            }
        }
    }

    private fun mountChipGroup(group: ChipGroup, listName: List<String>) {
        group.removeAllViews()
        group.isVisible = listName.isNotEmpty()
        listName.forEach { type ->
            group.addView(createChipItem(type))
        }
    }

    private fun createChipItem(textValue: String): Chip {
        return Chip(requireContext()).apply {
            text = textValue
            chipEndPadding = 8F
            chipStartPadding = 8F
        }
    }

    override fun onDestroy() {
        viewModel.showBottomNav()
        super.onDestroy()
    }
}