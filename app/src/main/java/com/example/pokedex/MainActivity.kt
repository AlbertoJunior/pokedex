package com.example.pokedex

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pokedex.databinding.ActivityMainBinding
import com.example.pokedex.view.pokedex.viewmodel.PokemonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<PokemonViewModel>()

    private lateinit var binding: ActivityMainBinding
    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.navView.apply {
            setupWithNavController(navController)
        }

//        viewModel.loadingStatus.observe(this) {
//            binding.progress.visibility = it
//        }

        viewModel.bottomNavigationStatus.observe(this) {
            binding.navView.visibility = it
        }
    }
}