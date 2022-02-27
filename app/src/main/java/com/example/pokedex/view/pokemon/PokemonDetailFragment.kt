package com.example.pokedex.view.pokemon

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.GridLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pokedex.R
import com.example.pokedex.core.EventSource
import com.example.pokedex.core.Utils
import com.example.pokedex.core.capitalize
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
    private lateinit var navController: NavController

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
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.hideNav) {
            viewModel.hideBottomNav()
        }

        if (args.hideButtons) {
            binding.containerButtons.visibility = View.GONE
        }

        setupListeners()
        loadPokemon()
        callObservers()
    }

    private fun callObservers() {
        viewModelDetails.favoriteEvent.observe(viewLifecycleOwner) { eventSource ->
            eventSource?.let { event ->
                event.message?.let { message ->
                    if (event is EventSource.Ready) {
                        val anim = if (event.value) R.anim.fade_in else R.anim.fade_out
                        AnimationUtils.loadAnimation(requireContext(), anim).also { animation ->
                            animation.duration = 300
                            binding.ivImageFavorite.animation = animation
                        }.start()
                    }
                }
                viewModelDetails.clearFavoriteEvent()
            }
        }

        viewModelDetails.loadingStatus.observe(viewLifecycleOwner) {
            binding.loadingStatus = it
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

        binding.btFlee.setOnClickListener {
            navController.navigateUp()
        }

        binding.btBack.setOnClickListener {
            navController.navigateUp()
        }
    }

    private fun loadPokemon() {
        viewModelDetails.fetchPokemonUltraDetail(args.pokemonId).observe(viewLifecycleOwner) {
            binding.pokemonDetail = it

            binding.ivImageFavorite.visibility = if (it.favorite) View.VISIBLE else View.GONE

            mountChipGroup(binding.cgType, it.types)
            mountChipGroup(
                binding.cgEncounterGroup,
                it.pokemonArea.map { area -> area.name.replace("-", " ") }
            )
            mountStats(it)
            mountMoves(it)

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

    private fun mountMoves(pokemon: Pokemon) {
        pokemon.moves?.let { moves ->
            if (moves.isNotEmpty()) {
                binding.glMoves.removeAllViews()
                binding.glMoves.visibility = View.VISIBLE
            } else {
                binding.glMoves.visibility = View.GONE
            }

            moves.forEach { move ->
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
                binding.glMoves.addView(button)
            }
        }
    }

    private fun mountStats(pokemon: Pokemon) {
        pokemon.stats?.let { stats ->
            if (stats.isNotEmpty()) {
                binding.llStatsGroup.removeAllViews()
                binding.llStatsGroup.visibility = View.VISIBLE
            } else {
                binding.llStatsGroup.visibility = View.GONE
            }

            stats.forEach {
                val textView = MaterialTextView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    textSize = 14F

                    val statsName = it.name.replace("-", " ").capitalize()
                    text = getString(R.string.stat_format, statsName, it.base.toString())
                }
                binding.llStatsGroup.addView(textView)
            }
        }
    }

    private fun mountChipGroup(group: ChipGroup, listName: List<String>) {
        if (listName.isNotEmpty()) {
            group.removeAllViews()
            group.visibility = View.VISIBLE
        } else {
            group.visibility = View.GONE
        }

        listName.forEach { type ->
            group.addView(createChipItem(type))
        }
    }

    private fun createChipItem(textValue: String): Chip {
        return Chip(requireContext()).apply {
            text = textValue.capitalize()
            chipEndPadding = 8F
            chipStartPadding = 8F
        }
    }

    override fun onDestroy() {
        viewModel.showBottomNav()
        super.onDestroy()
    }
}