package com.example.pokedex.view.pokemon

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Transformations
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.core.EventSource
import com.example.pokedex.core.Utils
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.databinding.FragmentPokemonDetailsBinding
import com.example.pokedex.view.pokedex.adapter.GenericAdapter
import com.example.pokedex.view.pokedex.viewmodel.PokemonViewModel
import com.example.pokedex.view.pokemon.viewmodel.PokemonDetailViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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
            hideButtons: Boolean = true,
            hideBtBack: Boolean = false
        ) = PokemonDetailFragment().apply {
            arguments = bundleOf(
                "pokemon_id" to pokemonId,
                "hide_nav" to hideNav,
                "hide_buttons" to hideButtons,
                "hide_bt_back" to hideBtBack
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPokemonDetailsBinding.inflate(inflater, container, false)
        viewModel.setPokemonId(args.pokemonId)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.hideNav) {
            viewModel.hideBottomNav()
        }

        binding.containerButtons.isVisible = !args.hideButtons
        binding.auxPadding.isVisible = !args.hideButtons
        binding.btBack.isVisible = !args.hideBtBack

        callObservers()
        loadPokemon()
    }

    private fun defaultLoading(
        eventSource: EventSource<Boolean>?,
        idIcon: Int,
        listenerOnAnimationEnd: () -> Unit
    ) {
        binding.mcLoadContainer.isVisible = eventSource != null
        binding.progress.isVisible = eventSource is EventSource.Loading
        binding.ivInfoLoading.isVisible = false

        eventSource?.let { event ->
            binding.tvLoadMessage.text = event.message ?: ""

            if (event !is EventSource.Loading) {
                binding.ivInfoLoading.isVisible = true
                binding.ivInfoLoading.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), idIcon)
                )

                if (event is EventSource.Ready) {
                    Utils.fade(event.value, binding.ivImageFavorite)
                }

                Utils.fade(
                    false,
                    binding.mcLoadContainer, binding.ivInfoLoading,
                    duration = 800,
                    delay = 800,
                    listenerOnAnimationEnd = listenerOnAnimationEnd
                )
            }
        }
    }

    private fun callObservers() {
        viewModelDetails.favoriteEvent.observe(viewLifecycleOwner) { eventSource ->
            val icon = when (eventSource) {
                is EventSource.Error -> R.drawable.ic_alert_circle
                is EventSource.Ready -> if (eventSource.value) R.drawable.ic_pokeball else R.drawable.ic_flee
                else -> R.drawable.ic_alert_circle
            }
            defaultLoading(eventSource, icon) { viewModelDetails.clearFavoriteEvent() }
        }

        viewModelDetails.sendInfoEvent.observe(viewLifecycleOwner) { eventSource ->
            val icon = when (eventSource) {
                is EventSource.Error -> R.drawable.ic_alert_circle
                is EventSource.Ready -> R.drawable.ic_cloud_upload
                else -> R.drawable.ic_alert_circle
            }
            defaultLoading(eventSource, icon) { viewModelDetails.clearSendEvent() }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners(pokemonId: Long) {
        binding.btCatch.setOnClickListener {
            viewModelDetails.savePokemonFavorite(pokemonId)
        }

        binding.btFlavor.setOnClickListener {
//            MaterialAlertDialogBuilder(requireContext())
//                .setMessage()
//                .setPositiveButton("Ok", null)
//                .show()
        }

        binding.btHelp.setOnClickListener {
            binding.ivInfoLoading.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_help)
            )
            binding.tvLoadMessage.text = getString(R.string.double_tap_message)
            binding.mcLoadContainer.isVisible = true
            binding.ivInfoLoading.isVisible = true
            Utils.fade(
                false,
                binding.mcLoadContainer, binding.ivInfoLoading,
                duration = 800,
                delay = 1300,
            )
        }

        val gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(p0: MotionEvent?): Boolean {
                    viewModelDetails.savePokemonFavorite(pokemonId)
                    return true
                }
            })
        binding.ivImage.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        binding.btSend.setOnClickListener {
            viewModelDetails.sendPokemonInfo(pokemonId)
        }

        binding.btBack.setOnClickListener {
            if (args.hideButtons)
                requireActivity().onBackPressed()
            else
                navController.navigateUp()
        }
    }

    private fun loadPokemon() {
        binding.progress.isVisible = true
        binding.progressImage.isVisible = true

        Transformations.switchMap(viewModel.pokemonId) {
            viewModelDetails.fetchPokemonDetail(it ?: args.pokemonId)
        }.observe(viewLifecycleOwner) {
            setupListeners(it.id)
            binding.pokemonDetail = it

            binding.ivImageFavorite.isVisible = it.favorite

            Utils.loadImageGlide(
                requireContext(), it.getImage(), binding.ivImage, R.drawable.ic_pokeball,
                listenerOnReady = {
                    binding.progress.isVisible = false
                    binding.progressImage.isVisible = false
                },
                listenerOnError = {
                    binding.progress.isVisible = false
                    binding.progressImage.isVisible = false
                })

            mountChipGroup(binding.cgEncounterGroup, it.pokemonArea.map { area -> area.name })
            mountGridGroup(it.pokemonSpecie?.color, it.types, binding.rvTypes)
            mountGridGroup(it.pokemonSpecie?.color, it.abilities, binding.rvAbilities)
            mountGridGroup(it.pokemonSpecie?.color, it.moves, binding.rvMoves)
            mountStats(it)
        }
    }

    private fun mountGridGroup(
        colorText: String?,
        list: List<String>?,
        gridLayout: RecyclerView,
        onClick: View.OnClickListener? = null
    ) {
        GenericAdapter(colorText, onClick).apply {
            gridLayout.adapter = this
            submitList(list)
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