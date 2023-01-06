package com.example.pokedex.view.pokemon

import android.annotation.SuppressLint
import android.content.Intent
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
import com.example.pokedex.NavigationDirections
import com.example.pokedex.R
import com.example.pokedex.core.EventSource
import com.example.pokedex.core.Utils
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.data.local.model.Stat
import com.example.pokedex.databinding.FragmentPokemonDetailsBinding
import com.example.pokedex.view.dialog.listeners.GenericAdapterClickListener
import com.example.pokedex.view.pokedex.viewmodel.PokemonViewModel
import com.example.pokedex.view.pokemon.adapter.GenericAdapter
import com.example.pokedex.view.pokemon.adapter.GenericTextAdapter
import com.example.pokedex.view.pokemon.viewmodel.PokemonDetailViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokemonDetailFragment : Fragment() {
    private lateinit var binding: FragmentPokemonDetailsBinding
    private val navController by lazy { findNavController() }

    private val args by navArgs<PokemonDetailFragmentArgs>()
    private val viewModel by activityViewModels<PokemonViewModel>()
    private val viewModelDetails by activityViewModels<PokemonDetailViewModel>()

    companion object {
        private const val POKEMON_ID = "pokemon_id"
        private const val HIDE_NAV = "hide_nav"
        private const val HIDE_BUTTONS = "hide_buttons"
        private const val HIDE_BT_BACK = "hide_bt_back"

        fun newInstance(
            pokemonId: Long,
            hideNav: Boolean = false,
            hideButtons: Boolean = true,
            hideBtBack: Boolean = false
        ) = PokemonDetailFragment().apply {
            arguments = bundleOf(
                POKEMON_ID to pokemonId,
                HIDE_NAV to hideNav,
                HIDE_BUTTONS to hideButtons,
                HIDE_BT_BACK to hideBtBack
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPokemonDetailsBinding.inflate(inflater, container, false)
        verifyHasDeeplink()
        return binding.root
    }

    private fun verifyHasDeeplink() {
        val intent = requireActivity().intent
        val pokemonId = if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.path?.substringAfterLast('/')?.toLong() ?: args.pokemonId
        } else {
            args.pokemonId
        }
        viewModel.setPokemonId(pokemonId)
        intent.data = null
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
        binding.cpProgressGeneral.isVisible = eventSource is EventSource.Loading
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
            navController.navigate(
                NavigationDirections.actionNavigationPokemonDetailsToShowPokemonFlavorText(pokemonId)
            )
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
                override fun onDoubleTap(p0: MotionEvent): Boolean {
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
                requireActivity().onBackPressedDispatcher.onBackPressed()
            else
                navController.navigateUp()
        }
    }

    private fun loadPokemon() {
        Transformations.switchMap(viewModel.pokemonId) {
            val id = it ?: args.pokemonId
            binding.progress.isVisible = true
            binding.progressImage.isVisible = true
            binding.cpProgressGeneral.isVisible = true
            viewModelDetails.fetchPokemonDetail(id)
        }.observe(viewLifecycleOwner) {
            binding.pokemonDetail = it
            binding.ivImageFavorite.isVisible = it.favorite

            setupListeners(it.id)

            loadImage(it)

            val pokemonColor = it.pokemonSpecie?.color

            mountEncounterArea(pokemonColor, it)
            mountTypes(pokemonColor, it.types)
            mountAbilities(pokemonColor, it.abilities)
            mountMoves(pokemonColor, it.moves)
            mountStats(pokemonColor, it.stats)

            binding.cpProgressGeneral.isVisible = false
        }
    }

    private fun mountTypes(pokemonColor: String?, types: List<String>?) {
        val typeList = types?.takeIf { check -> check.isNotEmpty() }
            ?: listOf(getString(R.string.not_found_information))
        mountGridGroup(pokemonColor, typeList, binding.rvTypes)
    }

    private fun mountAbilities(pokemonColor: String?, abilities: List<String>?) {
        val abilityList = abilities?.takeIf { check -> check.isNotEmpty() }
            ?: listOf(getString(R.string.not_found_information))
        mountGridGroup(pokemonColor, abilityList, binding.rvAbilities)
    }

    private fun mountMoves(pokemonColor: String?, moves: List<String>?) {
        val moveList = moves?.takeIf { check -> check.isNotEmpty() }
            ?: listOf(getString(R.string.not_found_information))
        mountGridGroup(pokemonColor, moveList, binding.rvMoves)
    }

    private fun mountEncounterArea(pokemonColor: String?, pokemon: Pokemon) {
        val listArea = pokemon.pokemonArea.map { area -> area.name }

        val onClick = if (listArea.isNotEmpty()) {
            object : GenericAdapterClickListener {
                override fun onItemClick(itemPosition: Int) {
                    navController.navigate(
                        NavigationDirections.actionNavigationPokemonDetailsToShowPokemonArea(
                            pokemon.id,
                            itemPosition
                        )
                    )
                }
            }
        } else {
            object : GenericAdapterClickListener {
                override fun onItemClick(itemPosition: Int) {
                    Snackbar
                        .make(
                            binding.root,
                            R.string.message_not_found_pokemon_area,
                            Snackbar.LENGTH_SHORT
                        )
                        .setAnchorView(binding.rvEncounterGroup)
                        .show()
                }
            }
        }

        mountGridGroup(
            pokemonColor,
            listArea.takeIf { it.isNotEmpty() } ?: listOf(getString(R.string.not_found)),
            binding.rvEncounterGroup,
            onClick
        )
    }

    private fun loadImage(pokemon: Pokemon) {
        Utils.loadImageGlide(
            requireContext(),
            pokemon.getImage(),
            binding.ivImage,
            R.drawable.ic_pokeball,
            listenerOnReady = {
                binding.progress.isVisible = false
                binding.progressImage.isVisible = false
            },
            listenerOnError = {
                binding.progress.isVisible = false
                binding.progressImage.isVisible = false
            })
    }

    private fun mountGridGroup(
        colorText: String?,
        list: List<String>,
        gridLayout: RecyclerView,
        onClick: GenericAdapterClickListener? = null
    ) {
        GenericAdapter(colorText, onClick).apply {
            gridLayout.adapter = this
            submitList(list)
        }
    }

    private fun mountStats(color: String?, stats: List<Stat>?) {
        val statsList = stats ?: emptyList()
        val showStats = statsList.isNotEmpty()
        binding.tvStat.isVisible = showStats
        binding.rvStats.isVisible = showStats

        if (showStats)
            binding.rvStats.adapter = GenericTextAdapter(color).apply {
                submitList(statsList.map { stat ->
                    getString(R.string.stat_format, stat.name, stat.base)
                })
            }
    }

    override fun onDestroy() {
        viewModel.showBottomNav()
        super.onDestroy()
    }
}